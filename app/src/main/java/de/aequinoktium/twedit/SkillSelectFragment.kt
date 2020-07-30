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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*


/**
 * The SkillSelectFragment helps players to select new skills
 */
class SkillSelectFragment : Fragment(), SkillFilterDialog.SkillFilterDialogListener {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0
    private var skill_data = emptyArray<SkillData>()

    private var show_base = true
    private var show_skil = true
    private var show_spec = true
    private var show_act = true
    private var show_pas = true
    private lateinit var te_search: EditText
    private lateinit var rv_container: RecyclerView
    private lateinit var rv_adapter: SkillSelectAdapter
    private lateinit var rv_manager: RecyclerView.LayoutManager

    class SkillData {
        var id: Int = 0
        var name: String = ""
        var parent_id: Int = 0
        var spec: Int = 0
        var activated: Boolean = false
        var has_level: Boolean = false
        var is_active: Int = 0
        var in_list: Boolean = false
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
        rv_container = act.findViewById(R.id.skillselect_recyclerview)
        rv_manager = LinearLayoutManager(act)
        rv_adapter = SkillSelectAdapter(skill_data, c)
        rv_container.apply {
            setHasFixedSize(true)
            layoutManager = rv_manager
            adapter = rv_adapter
        }

        te_search = act.findViewById(R.id.skillselect_searchfield)
        te_search.addTextChangedListener(TextChanged(te_search))
        val filter_button = act.findViewById<Button>(R.id.skillselect_filter)
        filter_button.setOnClickListener {
            filter()
        }

        searchSkills()
    }

    /**
     * load the skills on the IO thread and update the adapter.
     */
    fun searchSkills() {
        c.viewModelScope.launch(Dispatchers.IO) {
            skill_data = loadSkills()
            withContext(Dispatchers.Main) {
                rv_adapter.updateData(skill_data)
            }
        }
    }

    /**
     * Load all available skills from the SQLite database
     * @return Array of SkillData
     */
    suspend fun loadSkills(): Array<SkillData> {
        var sql = "SELECT id, name, parent_id, spec, is_active FROM skills"
        val data: Cursor = c.db.rawQuery(sql, null)
        var skills = emptyArray<SkillData>()

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

        while (data.moveToNext()) {
            val skill = SkillData()
            skill.id = data.getInt(0)
            skill.name = data.getString(1)
            skill.parent_id = data.getInt(2)
            skill.spec = data.getInt(3)
            skill.is_active = data.getInt(4)

            if (skill.id in activated_skills) {
                skill.activated = true
                if (skill.id in actual_skills) {
                    skill.has_level = true
                }
            }
            skills += skill
        }

        data.close()
        char_data.close()

        // sort the tree
        val sorted = mutableListOf<SkillData>()
        for (spec in 1..3) {
            for (skill in skills) {
                if (skill.spec == 1) {
                    if (!skill.in_list) {
                        sorted.add(skill)
                        skill.in_list = true
                    }
                    continue
                } else {
                    for (i in 0..sorted.size - 1) {
                        if (skill.parent_id == sorted[i].id) {
                            if (!skill.in_list) {
                                sorted.add(i + 1, skill)
                                skill.in_list = true
                            }
                            continue
                        }
                    }
                }
            }
        }
        return sorted.toTypedArray()
    }

    /**
     * Toggle the visible skills based on search string and selection from SkillFilterDialog
     */
    fun toggleSkills() {
        val search = te_search.text.toString().toLowerCase(c.LOCALE)
        var filtered_skills = emptyArray<SkillData>()

        for (skill in skill_data) {
            var show = true
            if (search.length > 1 && search !in skill.name.toLowerCase(c.LOCALE)) {
                show = false
            }
            if (!show_base && skill.spec == 1) {
                show = false
            }
            if (!show_skil && skill.spec == 2) {
                show = false
            }
            if (!show_spec && skill.spec == 3) {
                show = false
            }
            if (!show_act && skill.is_active == 1) {
                show = false
            }
            if (!show_pas && skill.is_active == 0) {
                show = false
            }

            if (show) {
                filtered_skills += skill
            }
        }
        rv_adapter.updateData(filtered_skills)
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

    /**
     * Implements a [TextWatcher] to monitor the search field.
     */
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

