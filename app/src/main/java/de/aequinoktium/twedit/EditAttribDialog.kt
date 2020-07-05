package de.aequinoktium.twedit

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class EditAttribDialog(char_attrib: String, cur_value: Int): DialogFragment() {
    var char_attrib: String = char_attrib
    var cur_value: Int = cur_value
    var new_value: Int = cur_value
    var xp_cost: Int = 0

    internal lateinit var listener: EditAttribDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface EditAttribDialogListener {
        fun onEditAttribDialogPositiveClick(dialog: EditAttribDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as EditAttribDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement EditAttribDialogListener"))
        }
    }

    /**
     * Create the dialog
      */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle(R.string.dialog_attrib_edit_title)
            var inflater: LayoutInflater = this.layoutInflater
            var content: View = inflater.inflate(R.layout.dialog_edit_attrib, null)

            // attribute text
            var dialog_label = content.findViewById<TextView>(R.id.edit_attrib_label)
            dialog_label.text = char_attrib.toUpperCase()

            var dialog_xp = content.findViewById<TextView>(R.id.edit_attrib_xp_value)
            dialog_xp.text = "0"

            // setting up the spinner ...
            var dialog_spinner = content.findViewById<NumberPicker>(R.id.edit_attrib_number)
            dialog_spinner.minValue = 0
            dialog_spinner.maxValue = 12
            dialog_spinner.value = cur_value
            dialog_spinner.wrapSelectorWheel = false
            dialog_spinner.setOnValueChangedListener { spinner_view, old_val, new_val ->
                updateXp(dialog_xp, new_val)
                new_value = new_val
            }


            builder.setView(content)
            builder.setPositiveButton(R.string.dialog_ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onEditAttribDialogPositiveClick(this)

                    }
            )
            builder.setNegativeButton(R.string.dialog_cancel,
                    DialogInterface.OnClickListener{ dialog, id ->

                    }
            )
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    /**
     * Update the XP cost of the operation based on the old and new value
     * @param textView: a text view to be updated
     * @param new_value: the new value
     */
    fun updateXp(textView: TextView, new_value: Int) {
        var spent_xp = cur_value * (cur_value + 1)
        var new_cost = new_value * (new_value + 1)
        xp_cost = new_cost - spent_xp
        textView.text = xp_cost.toString()
    }



}