package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Math.floor

class SkillFilterDialog(
    var show_base: Boolean,
    var show_skil: Boolean,
    var show_spec: Boolean,
    var show_act: Boolean,
    var show_pas: Boolean
) : DialogFragment() {

    private lateinit var v_base: CheckBox
    private lateinit var v_skil: CheckBox
    private lateinit var v_spec: CheckBox
    private lateinit var v_act: CheckBox
    private lateinit var v_pas: CheckBox

    internal lateinit var listener: SkillFilterDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface SkillFilterDialogListener {
        fun onSkillFilterDialogPositiveClick(dialog: SkillFilterDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as SkillFilterDialogListener
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

            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_skill_filter, null)

            v_base = content.findViewById<CheckBox>(R.id.dsf_base)
            v_base.isChecked = show_base
            v_skil = content.findViewById<CheckBox>(R.id.dsf_skil)
            v_skil.isChecked = show_skil
            v_spec = content.findViewById<CheckBox>(R.id.dsf_spec)
            v_spec.isChecked = show_spec
            v_act = content.findViewById<CheckBox>(R.id.dsf_act)
            v_act.isChecked = show_act
            v_pas = content.findViewById<CheckBox>(R.id.dsf_pas)
            v_pas.isChecked = show_pas

            val bx = arrayOf(v_base, v_skil, v_spec, v_act, v_pas)
            for (v in bx) {
                v.setOnCheckedChangeListener{v, value -> selected(v, value)}
            }

            builder.setTitle(R.string.dialog_skill_filter_title)
            builder.setView(content)
            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onSkillFilterDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    /**
     * OnCheckedChangedListener for the Checkboxes
     */
    fun selected(v: View, value: Boolean) {
        when (v) {
            v_base -> show_base = value
            v_skil -> show_skil = value
            v_spec -> show_spec = value
            v_act -> show_act = value
            v_pas -> show_pas = value
        }
    }
}

