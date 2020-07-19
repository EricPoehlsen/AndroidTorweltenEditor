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

/**
 * This dialog is used to confirm if the user wants to
 * collapse the selected trait variants into the editable text
 * in the CharTraitEditFragment
 */
class CollapseTraitDialog: DialogFragment() {
    internal lateinit var listener: CollapseTraitDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface CollapseTraitDialogListener {
        fun onCollapseTraitDialogPositiveClick(dialog: CollapseTraitDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as CollapseTraitDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement RemoveDialogListener"))
        }
    }

    /**
     * Create the dialog
      */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setTitle(R.string.dialog_collapse_trait_title)
            builder.setMessage(R.string.dialog_collapse_trait_msg)

            builder.setPositiveButton(R.string.dialog_ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onCollapseTraitDialogPositiveClick(this)

                    }
            )
            builder.setNegativeButton(R.string.dialog_cancel, null)
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }



}