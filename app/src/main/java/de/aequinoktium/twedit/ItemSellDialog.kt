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

/**
 * A DialogFrament that is used to modify an item damage code
 * @param item is the current item
 * @param c is the [CharacterViewModel] for this app.
 */
class ItemSellDialog(val item: Item): DialogFragment()
{
    internal lateinit var listener: DialogListener
    private lateinit var et_p: EditText
    var p = 0f

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
            val content: View = inflater.inflate(R.layout.dialog_item_sell, null)

            et_p = content.findViewById(R.id.dia_item_sell_price)
            et_p.setText(item.price.toString())
            et_p.addTextChangedListener(TextChanged(et_p, this))
            p = item.price

            val tv_info = content.findViewById<TextView>(R.id.dia_item_sell_info)
            if (item.qty == 1) tv_info.visibility = View.GONE

            builder.setView(content)

            val title = getString(R.string.dialog_item_sell_title, item.qty, item.name)

            builder.setTitle(title)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }


    /**
     * Implementation of the [TextWatcher]
     */
    class TextChanged(var et: EditText, val dialog: ItemSellDialog): TextWatcher {

        override fun afterTextChanged(text: Editable?) {
            val s = et.text.toString()
            if (s.matches("0\\d+(.\\d+)?".toRegex())) {
                dialog.p = s.toFloat()
            }
        }

        // necessary implementations for TextWatcher
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

}

