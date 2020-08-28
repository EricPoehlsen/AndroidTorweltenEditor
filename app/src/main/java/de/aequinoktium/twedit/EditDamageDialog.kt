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

class EditDamageDialog(
): DialogFragment() {
    var new_value: Int = 0
    var xp_cost: Int = 0

    internal lateinit var listener: DamageDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DamageDialogListener {
        fun onDamageDialogPositiveClick(dialog: EditDamageDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as DamageDialogListener
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
            val content: View = inflater.inflate(R.layout.dialog_edit_damage, null)

            builder.setView(content)
            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDamageDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

}

