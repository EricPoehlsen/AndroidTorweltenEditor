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

class RemoveTraitDialog(trait_name: String): DialogFragment() {
    var trait_name = trait_name
    internal lateinit var listener: RemoveTraitDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface RemoveTraitDialogListener {
        fun onRemoveTraitDialogPositiveClick(dialog: RemoveTraitDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as RemoveTraitDialogListener
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

            var msg = getString(R.string.dialog_remove_trait_msg, trait_name)
            builder.setIcon(R.drawable.remove_red)
            builder.setTitle(R.string.dialog_remove_trait_title)
            builder.setMessage(msg)

            builder.setPositiveButton(R.string.dialog_ok,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onRemoveTraitDialogPositiveClick(this)

                    }
            )
            builder.setNegativeButton(R.string.dialog_cancel, null)
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }



}