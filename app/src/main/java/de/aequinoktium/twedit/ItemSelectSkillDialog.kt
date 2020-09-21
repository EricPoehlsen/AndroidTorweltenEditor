package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament that is used to select a skill to use with this item
 */
class ItemSelectSkillDialog(): DialogFragment()
{
    internal lateinit var listener: DialogListener
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var ll_container: LinearLayout
    var selected = 0

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as DialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implement DialogListener")
            )
        }
    }

    /**
     * Create the dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_item_skill, null)

            ll_container = content.findViewById<LinearLayout>(R.id.dia_item_skill_container)
            showSkills()


            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun showSkills() {
        var skills = arrayOf<CharacterViewModel.Skill>(
            CharacterViewModel.Skill().apply {
                name = getString(R.string.dialog_item_skill_none)
                id = 0
            }
        )
        skills += c.char_skills

        for (s in skills) {
            val sv = SkillView(context)
            sv.skill_id = s.id
            sv.tv.text = s.name
            sv.iv.setImageResource(sv.skill_levels[s.lvl])
            sv.setOnClickListener {v -> select(v)}
            ll_container.addView(sv)
        }
    }

    fun select(view: View) {
        val grey = ContextCompat.getColor(requireContext(),R.color.Grey)
        val blue = ContextCompat.getColor(requireContext(),R.color.Blue)

        for (v in ll_container.children) {
            v as SkillView
            v.tv.setTextColor(grey)
        }

        view as SkillView
        view.tv.setTextColor(blue)
        selected = view.skill_id
    }

    class SkillView(context: Context?): LinearLayout(context) {
        val skill_levels = arrayOf(
            R.drawable.pips_3_0,
            R.drawable.pips_3_1,
            R.drawable.pips_3_2,
            R.drawable.pips_3_3
        )

        var skill_id = 0
        val iv = ImageView(context)
        val tv = TextView(context)

        // calculate px for dp value
        fun px(dp: Int): Float = dp * resources.displayMetrics.density

        init {
            val lp = LinearLayout.LayoutParams(px(16).toInt(), px(16).toInt())
            lp.setMargins(0,0, px(8).toInt(), 0)
            iv.layoutParams = lp
            this.addView(iv)

            this.setPadding(
                px(8).toInt(),
                px(8).toInt(),
                px(8).toInt(),
                px(8).toInt()
            )

            this.addView(tv)
        }
    }




}

