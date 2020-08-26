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
 * A DialogFrament that is used to modify an item damage code
 * @param item is the current item
 * @param c is the [CharacterViewModel] for this app.
 */
class ItemQualDialog(var q: Int = 0): DialogFragment()
{
    internal lateinit var listener: DialogListener
    private lateinit var tv_q: TextView

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
            val content: View = inflater.inflate(R.layout.dialog_item_qual, null)

            val bt_inc = content.findViewById<Button>(R.id.dia_item_qual_inc)
            bt_inc.setOnClickListener{modQuality(+1)}

            val bt_dec = content.findViewById<Button>(R.id.dia_item_qual_dec)
            bt_dec.setOnClickListener{modQuality(-1)}

            tv_q = content.findViewById(R.id.dia_item_qual)

            modQuality(0)

            builder.setView(content)

            val title = getString(R.string.dialog_item_qual_title)

            builder.setTitle(title)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun modQuality(delta: Int) {
        q += delta
        if (q > 12) q = 12
        if (q < 0) q = 0

        val qualities = resources.getStringArray(R.array.cinv_qualities)
        var text = "${qualities[q]} ($q)"
        tv_q.text = text
    }
}

