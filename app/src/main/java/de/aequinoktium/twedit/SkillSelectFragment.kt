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

/**
 * The SkillSelectFragment helps players to select new skills
 */
class SkillSelectFragment : Fragment() {
    var char_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            char_id = it.getInt("char_id",0)
        }
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
        var act = activity as MainActivity
        var search_input = act.findViewById<EditText>(R.id.skillselect_searchfield)
        var search_button = act.findViewById<Button>(R.id.skillselect_search)
        search_button.setOnClickListener {
            findSkills(search_input)
        }
    }

    fun findSkills(input: EditText) {
        // select skills from global skill list based on search
        var search = input.text.toString()
        search = search.replace("'", "\u2019")
        var sql = "SELECT id, name, base_skill, skill FROM skills WHERE name LIKE '%" + search + "%'"
        var act = activity as MainActivity
        var data: Cursor = act.db.rawQuery(sql, null)

        // select all skills the character has activated
        sql = "SELECT skill_id, lvl FROM char_skills WHERE char_id = " + char_id
        var char_data: Cursor = act.db.rawQuery(sql, null)
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

        var containerview = act.findViewById<LinearLayout>(R.id.skillselect_container)
        containerview.removeAllViews()
        if (data.count > 0) {
            data.moveToFirst()
            do {
                var list_entry = SkillSelectView(context)
                list_entry.text = data.getString(1)
                var skill_id = data.getInt(0)
                list_entry.skill_id = skill_id

                // format base skills and specialties
                // if field base skill == 0 it is a base_skill
                if (data.getInt(2) == 0) {
                    list_entry.setTypeface(null, Typeface.BOLD)
                }
                // if field id != skill it is a specialty
                if (data.getInt(0) != data.getInt(3) && data.getInt(2) != 0) {
                    list_entry.setTypeface(null, Typeface.ITALIC)
                }

                // set activated skills checked
                if (skill_id in activated_skills) {
                    list_entry.isChecked = true
                }

                // disable skills in which the character has a level
                if (skill_id in actual_skills) {
                    list_entry.isEnabled = false
                }
                
                list_entry.setOnCheckedChangeListener{ button, b ->
                    setSkill(list_entry)
                }

                containerview.addView(list_entry)
            } while (data.moveToNext())
        }
        data.close()
        char_data.close()
    }

    fun setSkill(ssv: SkillSelectView) {
        val skill_id = ssv.skill_id.toString()
        val act = activity as MainActivity

        if (ssv.isChecked) { // add skill
            var data = ContentValues()
            data.put("char_id", char_id)
            data.put("skill_id", skill_id)
            act.db.insert("char_skills", null, data)
        } else { // remove skill
            val s_str = skill_id.toString()
            val c_str = char_id.toString()
            var sql = "DELETE FROM char_skills WHERE skill_id = $s_str AND char_id = $c_str"
            act.db.execSQL(sql)
        }
    }

    companion object {
        /**
         * This factory method creates an instance of the skill select fragment
         *
         * @param char_id character id
         * @return A new instance of fragment SkillSelectFragment.
         */
        @JvmStatic
        fun newInstance(char_id: Int) =
            SkillSelectFragment().apply {
                arguments = Bundle().apply {
                    putInt("char_id", char_id)
                }
            }
    }
}