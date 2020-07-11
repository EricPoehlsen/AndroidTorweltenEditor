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
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

/**
 * The SkillSelectFragment helps players to select new skills
 */
class CharSkillFragment : Fragment(), EditSkillDialog.EditSkillDialogListener {
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
        return inflater.inflate(R.layout.fragment_char_skill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var act = activity as MainActivity

        // switch to skill selector fragment
        var b_add_skills = act.findViewById<Button>(R.id.charskills_add)
        b_add_skills.setOnClickListener {
            val bundle: Bundle = bundleOf("char_id" to char_id)
            this.findNavController().navigate(R.id.action_cs_to_ss, bundle)
        }
        displaySkills()
    }

    fun displaySkills() {

        var act = activity as MainActivity

        var skill_container = act.findViewById<LinearLayout>(R.id.charskills_layout)
        skill_container.removeAllViews()

        var sql: String = """                            
            SELECT char_skills.skill_id as id, 
                   char_skills.lvl as lvl,
                   skills.name as name, 
                   skills.base_skill as base_skill,
                   skills.skill as skill, 
                   skills.is_active as active 
                   FROM char_skills
                   JOIN skills 
                   ON char_skills.skill_id = skills.id
                   WHERE char_skills.char_id = $char_id
        """.trimIndent()
        var skills: Cursor = act.db.rawQuery(sql, null)

        if (skills.count > 0) {
            skills.moveToFirst()
            do {
                val skill_id = skills.getInt(0)
                val skill_name = skills.getString(2)
                val skill_lvl = skills.getInt(1)
                val base_id = skills.getInt(3)
                val skill_id2 = skills.getInt(4)
                val is_active = skills.getInt(5)

                var sk_line = LinearLayout(context)
                sk_line.orientation = LinearLayout.HORIZONTAL
                sk_line.tag = skill_id.toString() + "_line"

                var sk_lvl = ImageView(context)
                // set size, margins and gravity ...
                val margins = act.calc_dp(4)
                val img_size = act.calc_dp(16)
                var lp = LinearLayout.LayoutParams(img_size, img_size)
                lp.gravity = Gravity.CENTER_VERTICAL
                lp.setMargins(margins, 0, margins, 0)
                sk_lvl.layoutParams = lp

                var sk_type = ImageView(context)
                lp.setMargins(0,0,margins,0)
                sk_type.layoutParams = lp
                if (is_active == 1) {
                    sk_type.setImageResource(R.drawable.hand)
                } else {
                    sk_type.setImageResource(R.drawable.think)
                }

                // image based on value ...
                when {
                    skill_lvl == 1 -> sk_lvl.setImageResource(R.drawable.pips_3_1)
                    skill_lvl == 2 -> sk_lvl.setImageResource(R.drawable.pips_3_2)
                    skill_lvl == 3 -> sk_lvl.setImageResource(R.drawable.pips_3_3)
                    else -> sk_lvl.setImageResource(R.drawable.pips_3_0)
                }

                // set the skill name tag
                var sk_text = TextView(context)
                sk_text.text = skill_name
                if (base_id == 0) { // is base skill
                    sk_text.setTypeface(null, Typeface.BOLD)
                } else if (skill_id != skill_id2) { // is specialty
                    sk_text.setTypeface(null, Typeface.ITALIC)
                }
                val pdd = act.calc_dp(6)
                sk_text.setPadding(0,pdd,0,pdd)
                sk_text.tag = skill_id.toString() + "_text"

                sk_line.addView(sk_lvl)
                sk_line.addView(sk_type)
                sk_line.addView(sk_text)

                sk_line.setOnClickListener {
                    editSkill(skill_id, skill_lvl)
                }

                skill_container.addView(sk_line)
            } while (skills.moveToNext())
        }

        skills.close()
    }

    fun editSkill(char_skill: Int, cur_value: Int) {
        val act = activity as MainActivity
        val fm = this.parentFragmentManager
        val dialog = EditSkillDialog(char_id, char_skill, cur_value)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    // update skill when user confirms dialog
    override fun onEditSkillDialogPositiveClick(dialog: EditSkillDialog) {
        val act = activity as MainActivity
        var sql = ""
        val skill_id = dialog.skill_id
        val skill_lvl = dialog.new_value
        val xp_cost = dialog.xp_cost

        // update or remove skill ...
        if (skill_lvl >= 0) {
            sql = """
                UPDATE char_skills
                SET lvl = $skill_lvl
                WHERE char_id = $char_id AND skill_id = $skill_id
            """.trimIndent()
        } else {
            sql = """
                DELETE FROM char_skills
                WHERE char_id = $char_id AND skill_id = $skill_id
            """.trimIndent()
        }
        act.db.execSQL(sql)

        // update xp ...
        sql = """
            UPDATE char_core
            SET xp_used = xp_used + $xp_cost
            WHERE id = $char_id
        """.trimIndent()
        act.db.execSQL(sql)

        displaySkills()
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
            CharSkillFragment().apply {
                arguments = Bundle().apply {
                    putInt("char_id", char_id)
                }
            }
    }


}