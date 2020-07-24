package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.children
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.util.*

/**
 * The SkillSelectFragment helps players to select new skills
 */
class SkillSelectFragment : Fragment(), SkillFilterDialog.SkillFilterDialogListener {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0

    private var show_base = true
    private var show_skil = true
    private var show_spec = true
    private var show_act = true
    private var show_pas = true
    private lateinit var te_search: EditText
    private lateinit var ll_container: LinearLayout

    class SkillData {
        var id: Int = 0
        var name: String = ""
        var base_skill: Boolean = false
        var specialty: Boolean = false
        var activated: Boolean = false
        var has_level: Boolean = false
        var is_active: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_skill_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        char_id = c.char_id
        val act = activity as MainActivity


        c.viewModelScope.launch {
            val skills = loadSkills()
            withContext(Dispatchers.Main) {
                displaySkills(skills)
            }
        }

        ll_container = act.findViewById(R.id.skillselect_container)
        te_search = act.findViewById(R.id.skillselect_searchfield)
        te_search.addTextChangedListener(TextChanged(te_search))
        val filter_button = act.findViewById<Button>(R.id.skillselect_filter)
        filter_button.setOnClickListener {
            filter()
        }
    }

    fun searchSkills(input: String) {
        c.viewModelScope.launch {
            val skills = loadSkills()
            withContext(Dispatchers.Main) {
                displaySkills(skills)
            }
        }
    }

    /**
     * Load all available skills from the SQLite database
     * @return Array of SkillData
     */
    fun loadSkills(): Array<SkillData> {
        var sql = "SELECT id, name, base_skill, skill, is_active FROM skills"
        val data: Cursor = c.db.rawQuery(sql, null)

        // select all skills the character has activated
        sql = "SELECT skill_id, lvl FROM char_skills WHERE char_id = " + char_id
        val char_data: Cursor = c.db.rawQuery(sql, null)
        var activated_skills = arrayOf<Int>()
        var actual_skills = arrayOf<Int>()
        while (char_data.moveToNext()) {
            val skill = char_data.getInt(0)
            activated_skills += skill
            if (char_data.getInt(1) > 0) {
                actual_skills += skill
            }
        }

        var result = emptyArray<SkillData>()

        while (data.moveToNext()) {
            val skill = SkillData()
            skill.id = data.getInt(0)
            skill.name = data.getString(1)
            skill.is_active = data.getInt(4)
            Log.d("info", "${skill.name} : ${skill.is_active}")

            if (data.getInt(2) == 0) {
                skill.base_skill = true
            } else if (data.getInt(0) != data.getInt(3)) {
                skill.specialty = true
            }

            if (skill.id in activated_skills) {
                skill.activated = true
                if (skill.id in actual_skills) {
                    skill.has_level = true
                }
            }
            result += skill
        }

        data.close()
        char_data.close()

        return result
    }

    fun displaySkills(skills: Array<SkillData>) {
        val act = activity as MainActivity
        val containerview = act.findViewById<LinearLayout>(R.id.skillselect_container)

        for (skill in skills) {
            val list_entry = SkillSelectView(context)
            list_entry.text = skill.name
            list_entry.skill_id = skill.id

            if (skill.base_skill) {
                list_entry.is_base = true
                list_entry.is_skil = false
                list_entry.setTypeface(null, Typeface.BOLD)
            } else if (skill.specialty) {
                list_entry.is_spec = true
                list_entry.is_skil = false
                list_entry.setTypeface(null, Typeface.ITALIC)
            }
            if (skill.is_active == 1) {
                Log.d("info", "Aktiv: ${skill.name}")
                list_entry.is_act = true
                list_entry.is_pas = false
            } else {
                Log.d("info", "Passiv: ${skill.name}")
                list_entry.is_act = false
                list_entry.is_pas = true
            }

            if (skill.activated) {
                list_entry.isChecked = true
            }

            if (skill.has_level) {
                list_entry.isEnabled = false
            }

            list_entry.setOnCheckedChangeListener{ button, b ->
                setSkill(list_entry)
            }

            containerview.addView(list_entry)
        }
    }

    fun setSkill(ssv: SkillSelectView) {
        val skill_id = ssv.skill_id.toString()

        if (ssv.isChecked) { // add skill
            c.viewModelScope.launch {
                val data = ContentValues()
                data.put("char_id", char_id)
                data.put("skill_id", skill_id)
                c.db.insert("char_skills", null, data)
            }
        } else { // remove skill
            c.viewModelScope.launch {
                val s_str = skill_id.toString()
                val c_str = char_id.toString()
                val sql = "DELETE FROM char_skills WHERE skill_id = $s_str AND char_id = $c_str"
                c.db.execSQL(sql)
            }
        }
    }

    fun toggleSkills() {
        Log.d("info", "TOGGLE")
        var search = te_search.text.toString().toLowerCase(c.LOCALE)
        for (v in ll_container.children) {
            val cb = v as SkillSelectView
            cb.visibility = View.VISIBLE

            if (!show_base && cb.is_base) cb.visibility = View.GONE
            if (!show_skil && cb.is_skil) cb.visibility = View.GONE
            if (!show_spec && cb.is_spec) cb.visibility = View.GONE

            if (!show_act && cb.is_act) cb.visibility = View.GONE
            if (!show_pas && cb.is_pas) cb.visibility = View.GONE

            val name = cb.text.toString().toLowerCase(c.LOCALE)
            if (search.length >= 1 && search !in name) cb.visibility = View.GONE
        }
    }

    /**
     * Display the SkillFilterDialog
     */
    fun filter() {
        val fm = this.parentFragmentManager
        val dialog = SkillFilterDialog(show_base, show_skil, show_spec, show_act, show_pas)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    /**
     * Handle the result of the SkillFilterDialog
     */
    override fun onSkillFilterDialogPositiveClick(dialog: SkillFilterDialog) {
        show_base = dialog.show_base
        show_skil = dialog.show_skil
        show_spec = dialog.show_spec
        show_act = dialog.show_act
        show_pas = dialog.show_pas
        toggleSkills()
    }

    class TextChanged: TextWatcher {
        constructor(search: EditText) {
            this.search = search
            this.frgm = this.search.findFragment()
        }
        private var search: EditText
        private var frgm: SkillSelectFragment

        override fun afterTextChanged(p0: Editable?) {
            frgm.toggleSkills()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
}