package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Exception
import java.util.regex.Pattern
import kotlin.math.roundToInt

/**
 * A DialogFrament that is used to delete a character
 * @param char_id is the selected character
 */
class CharDeleteDialog(val char: DataViewModel.CharInfo): DialogFragment()
{
    internal lateinit var listener: DialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface DialogListener {
        fun onDialogPositiveClick(dialog: CharDeleteDialog)
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

            /*
            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_item_qual, null)
            builder.setView(content)
            */

            var name = char.name
            if (name.startsWith("#")) name = name.drop(1)
            val title = getString(R.string.dialog_del_char_title, name)
            builder.setTitle(title)
            builder.setIcon(R.drawable.remove_red)
            if (char.deleted) {
                builder.setMessage(R.string.dialog_del_char_warn)
            } else {
                builder.setMessage(R.string.dialog_del_char_text)
            }

            builder.setPositiveButton(R.string.dialog_del_char_del) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }
}

