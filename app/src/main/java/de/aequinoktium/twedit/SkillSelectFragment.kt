package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

/**
 * The SkillSelectFragment helps players to select new skills
 */
class SkillSelectFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0

    class SkillData {
        var id: Int = 0
        var name: String = ""
        var base_skill: Boolean = false
        var specialty: Boolean = false
        var activated: Boolean = false
        var has_level: Boolean = false
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
        val act = activity as MainActivity
        val search_input = act.findViewById<EditText>(R.id.skillselect_searchfield)
        val search_button = act.findViewById<Button>(R.id.skillselect_search)
        search_button.setOnClickListener {
            val input: String = search_input.text.toString()
            search(input)
        }
    }

    fun search(input: String) {
        c.viewModelScope.launch {
            val skills = findSkills(input)
            withContext(Dispatchers.Main) {
                updateView(skills)
            }
        }
    }

    fun findSkills(input: String): Array<SkillData> {
        // select skills from global skill list based on search
        val search = input.replace("'", "\u2019")

        var sql =
            "SELECT id, name, base_skill, skill FROM skills WHERE name LIKE '%" + search + "%'"
        val data: Cursor = c.db.rawQuery(sql, null)

        // select all skills the character has activated
        sql = "SELECT skill_id, lvl FROM char_skills WHERE char_id = " + char_id
        val char_data: Cursor = c.db.rawQuery(sql, null)
        var activated_skills = arrayOf<Int>()
        var actual_skills = arrayOf<Int>()
        for (i in 0 until char_data.count) {
            char_data.moveToNext()
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

    fun updateView(skills: Array<SkillData>) {
        val act = activity as MainActivity
        val containerview = act.findViewById<LinearLayout>(R.id.skillselect_container)
        containerview.removeAllViews()

        for (skill in skills) {
            val list_entry = SkillSelectView(context)
            list_entry.text = skill.name
            list_entry.skill_id = skill.id

            if (skill.base_skill) {
                list_entry.setTypeface(null, Typeface.BOLD)
            } else if (skill.specialty) {
                list_entry.setTypeface(null, Typeface.ITALIC)
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


}