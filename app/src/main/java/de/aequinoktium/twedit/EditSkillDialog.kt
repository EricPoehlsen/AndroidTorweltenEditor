package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Math.floor

class EditSkillDialog(
    var char_id: Int,
    var skill_id: Int,
    var cur_value: Int,
    val c: CharacterViewModel
): DialogFragment() {
    var new_value: Int = cur_value
    var xp_cost: Int = 0

    internal lateinit var listener: EditSkillDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface EditSkillDialogListener {
        fun onEditSkillDialogPositiveClick(dialog: EditSkillDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as EditSkillDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement EditSkillDialogListener")
            )
        }
    }

    /**
     * Create the dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val act = it as MainActivity

            val base_id = floor(skill_id.toDouble() / 10000).toInt() * 10000
            val skill_id2 = floor(skill_id.toDouble() / 100).toInt() * 100


            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_edit_skill, null)

            // retrieve the skill and parent skills from database
            val sql = """
                SELECT id, name, is_active 
                FROM skills 
                WHERE id = $skill_id OR
                      id = $base_id OR
                      id = $skill_id2
            """.trimIndent()
            val data: Cursor = c.db.rawQuery(sql, null)

            while (data.moveToNext()) {
                when {
                    // the selected skill:
                    data.getInt(0) == skill_id -> {
                        val title_txt = content.findViewById<TextView>(R.id.dia_skill_title)
                        title_txt.text = data.getString(1)
                        if (base_id == skill_id) { // is base skill
                            title_txt.setTypeface(null, Typeface.BOLD)
                        } else if (skill_id != skill_id2) { // is specialty
                            title_txt.setTypeface(null, Typeface.ITALIC)
                        }

                        val active_icon = content.findViewById<ImageView>(R.id.dia_skill_type)
                        if (data.getInt(2) == 1) {
                            active_icon.setImageResource(R.drawable.hand)
                        } else {
                            active_icon.setImageResource(R.drawable.think)
                        }

                    }
                }
            }

            val right_arrow = content.findViewById<ImageView>(R.id.dia_skill_right)
            right_arrow.setOnClickListener {
                changeLvl(1, content)
            }
            val left_arrow = content.findViewById<ImageView>(R.id.dia_skill_left)
            left_arrow.setOnClickListener {
                changeLvl(-1, content)
            }

            // set text and image to current value ...
            changeLvl(0, content)

            builder.setView(content)
            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onEditSkillDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    /**
     * Update the XP cost of the operation based on the old and new value
     * @param textView: a text view to be updated
     * @param new_value: the new value
     */
    fun updateXp(textView: TextView, new_value: Int) {
        val spent_xp = cur_value * (cur_value + 1)
        var new_cost = new_value * (new_value + 1)
        if (new_value < 0) new_cost = 0
        xp_cost = new_cost - spent_xp
        textView.text = xp_cost.toString()
    }

    // update the new_value and the views ...
    fun changeLvl(delta: Int, v: View) {
        new_value += delta
        if (new_value > 3) new_value = 3
        if (new_value < -1) new_value = -1
        val lvl = v.findViewById<ImageView>(R.id.dia_skill_lvl)
        val txt = v.findViewById<TextView>(R.id.dia_skill_lvl_txt)
        val xp = v.findViewById<TextView>(R.id.dia_skill_xp)
        updateXp(xp, new_value)
        when {
            new_value == -1 -> {
                lvl.setImageResource(R.drawable.remove_dark)
                txt.setText(R.string.dialog_edit_skill_del)
            }
            new_value == 0 -> {
                lvl.setImageResource(R.drawable.pips_3_0)
                txt.setText(R.string.dialog_edit_skill_lvl0)
            }
            new_value == 1 -> {
                lvl.setImageResource(R.drawable.pips_3_1)
                txt.setText(R.string.dialog_edit_skill_lvl1)
            }
            new_value == 2 -> {
                lvl.setImageResource(R.drawable.pips_3_2)
                txt.setText(R.string.dialog_edit_skill_lvl2)
            }
            new_value == 3 -> {
                lvl.setImageResource(R.drawable.pips_3_3)
                txt.setText(R.string.dialog_edit_skill_lvl3)
            }
        }
    }
}

