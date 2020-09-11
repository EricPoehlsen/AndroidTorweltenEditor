package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * A DialogFrament that is used to change inventory settings
 */
class CharInventorySettingsDialog(var packed: Boolean, var equipped: Boolean):
        DialogFragment(),
        CompoundButton.OnCheckedChangeListener
{

    internal lateinit var listener: DialogListener


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
            val content: View = inflater.inflate(R.layout.dialog_inventory_settings, null)
            val cb_packed = content.findViewById<CheckBox>(R.id.dia_invset_packed)
            cb_packed.isChecked = packed
            cb_packed.setOnCheckedChangeListener(this)
            val cb_equipped = content.findViewById<CheckBox>(R.id.dia_invset_equipped)
            cb_equipped.setOnCheckedChangeListener(this)
            cb_equipped.isChecked = equipped
            builder.setView(content)

            val title = getString(R.string.dialog_invset_title)

            builder.setTitle(title)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    override fun onCheckedChanged(button: CompoundButton?, state: Boolean) {
        if (button is CheckBox) {
            when (button.id) {
                R.id.dia_invset_equipped -> equipped = state
                R.id.dia_invset_packed -> packed = state
            }
        }
    }

}

