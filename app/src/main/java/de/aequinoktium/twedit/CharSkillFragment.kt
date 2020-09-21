package de.aequinoktium.twedit

import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * The SkillSelectFragment helps players to select new skills
 */
class CharSkillFragment : Fragment(),
                          EditSkillDialog.EditSkillDialogListener,
                          NewSkillDialog.NewSkillDialogListener {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_skill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var act = activity as MainActivity
        char_id = c.char_id

        // switch to skill selector fragment
        var b_add_skills = act.findViewById<Button>(R.id.charskills_add)
        b_add_skills.setOnClickListener {
            this.findNavController().navigate(R.id.action_cs_to_ss)
        }

        c.viewModelScope.launch(Dispatchers.IO) {
            c.loadSkills()
            withContext(Dispatchers.Main) {
                displaySkills(c.char_skills)
            }
        }
    }



    /**
     * Updates the View with the characters skills
     */
    fun displaySkills(skills: Array<CharacterViewModel.Skill>) {

        val act = activity as MainActivity
        val skill_container = act.findViewById<LinearLayout>(R.id.charskills_layout)
        skill_container.removeAllViews()

        for (skill in skills) {
            val sk_line = LinearLayout(context)
            sk_line.orientation = LinearLayout.HORIZONTAL
            sk_line.tag = skill.id.toString() + "_line"

            val sk_lvl = ImageView(context)
            // set size, margins and gravity ...
            val margins = act.calc_dp(4)
            val img_size = act.calc_dp(16)
            val lp = LinearLayout.LayoutParams(img_size, img_size)
            lp.gravity = Gravity.CENTER_VERTICAL
            lp.setMargins(margins, 0, margins, 0)
            sk_lvl.layoutParams = lp

            val sk_type = ImageView(context)
            lp.setMargins(0,0,margins,0)
            sk_type.layoutParams = lp
            if (skill.is_active) {
                sk_type.setImageResource(R.drawable.hand)
            } else {
                sk_type.setImageResource(R.drawable.think)
            }

            // image based on value ...
            when {
                skill.lvl == 1 -> sk_lvl.setImageResource(R.drawable.pips_3_1)
                skill.lvl == 2 -> sk_lvl.setImageResource(R.drawable.pips_3_2)
                skill.lvl == 3 -> sk_lvl.setImageResource(R.drawable.pips_3_3)
                else -> sk_lvl.setImageResource(R.drawable.pips_3_0)
            }

            // set the skill name tag
            val sk_text = TextView(context)
            sk_text.text = skill.name
            if (skill.spec == 1) sk_text.setTypeface(null, Typeface.BOLD)
            if (skill.spec == 3) sk_text.setTypeface(null, Typeface.ITALIC)

            val pdd = act.calc_dp(6)
            sk_text.setPadding(0,pdd,0,pdd)
            sk_text.tag = skill.id.toString() + "_text"

            sk_line.addView(sk_lvl)
            sk_line.addView(sk_type)
            sk_line.addView(sk_text)

            sk_line.setOnClickListener {
                editSkill(skill.id, skill.lvl)
            }
            sk_line.setOnLongClickListener {
                newSkill(skill.id)
            }

            skill_container.addView(sk_line)
        }
    }

    /**
     * Trigger the EditSkillDialog on a click
     */
    fun editSkill(char_skill: Int, cur_value: Int) {
        val fm = this.parentFragmentManager
        val dialog = EditSkillDialog(char_id, char_skill, cur_value, c)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    /**
     * Trigger the NewSkillDialog on a long click
     */
    fun newSkill(skill_id: Int): Boolean {
        val act = activity as MainActivity
        val fm = this.parentFragmentManager
        val dialog = NewSkillDialog(skill_id, c)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
        return true
    }

    /**
     * Create and activate a new skill
     */
    override fun onNewSkillDialogPositiveClick(dialog: NewSkillDialog) {
        c.viewModelScope.launch {
            createNewSkill(dialog.skill)
            c.loadSkills()
            withContext(Dispatchers.Main) {
                displaySkills(c.char_skills)
            }
        }
    }

    fun createNewSkill(skill: NewSkillDialog.SkillData) {
        var skill_id = 0
        skill.name = skill.name.replace("'", "\u2019")

        // check if skill exists
        var sql = """
            SELECT
                id 
            FROM 
                skills 
            WHERE 
                parent_id = ${skill.parent} AND name = '${skill.name}'
                """.trimIndent()
        var data = c.db.rawQuery(sql, null)
        if (data.moveToFirst()) {
            skill_id = data.getInt(0)
        }
        data.close()

        // add new skill to database and retrieve the id
        if (skill_id == 0) {
            sql = """
                INSERT INTO
                    skills
                    (name, parent_id, spec, is_active)
                VALUES
                    ('${skill.name}', ${skill.parent}, ${skill.spec}, ${skill.is_active})
            """.trimIndent()

            c.db.execSQL(sql)

            sql = """
                SELECT
                    id 
                FROM 
                    skills 
                WHERE 
                    parent_id = ${skill.parent} AND name = '${skill.name}'
                    """.trimIndent()
            data = c.db.rawQuery(sql, null)
            if (data.moveToFirst()) {
                skill_id = data.getInt(0)
            }
            data.close()
        }

        if (skill_id > 0) {
            // check if the skill is already selected:
            sql = """
                SELECT 
                    id 
                FROM 
                    char_skills 
                WHERE 
                    char_id = ${c.char_id} AND skill_id = $skill_id
            """.trimIndent()
            data = c.db.rawQuery(sql, null)
            if (data.count == 0) {
                sql = """
                    INSERT INTO 
                        char_skills
                        (char_id, skill_id)
                    VALUES
                        (${c.char_id}, $skill_id)
                """.trimIndent()
                c.db.execSQL(sql)
            }
            data.close()
        }
    }



    // update skill when user confirms dialog
    override fun onEditSkillDialogPositiveClick(dialog: EditSkillDialog) {
        val skill_id = dialog.skill_id
        val skill_lvl = dialog.new_value
        val xp_cost = dialog.xp_cost

        c.viewModelScope.launch {
            c.updateSkill(skill_id, skill_lvl)
            c.loadSkills()
            c.updateXP(xp_cost)
            withContext(Dispatchers.Main) {
                displaySkills(c.char_skills)
            }
        }
    }
}