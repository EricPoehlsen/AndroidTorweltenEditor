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
import kotlin.math.roundToInt

/**
 * A DialogFrament that is used to modify quantities of Items
 * @param info_id is the database id for the information to modify
 * @param c is the [CharacterViewModel] for this app.
 * @param v is the [View] calling this dialog
 */
class ItemQtyDialog(val cur_qty: Int): DialogFragment(),
                                   AdapterView.OnItemSelectedListener,
                                   TextWatcher
{
    internal lateinit var listener: DialogListener
    private lateinit var sp_select: Spinner
    private lateinit var et_amount: EditText
    var action = ""
    var qty = 0

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
            val content: View = inflater.inflate(R.layout.dialog_item_qty, null)

            sp_select = content.findViewById(R.id.dia_itemqty_spinner)
            val options = arrayOf(
                resources.getString(R.string.dialog_item_qty_buy),
                resources.getString(R.string.dialog_item_qty_take),
                resources.getString(R.string.dialog_item_qty_split),
                resources.getString(R.string.dialog_item_qty_part),
                resources.getString(R.string.dialog_item_qty_join)
            )
            val adapter = ArrayAdapter<String>(
                content.context,
                R.layout.support_simple_spinner_dropdown_item,
                options
            )
            sp_select.adapter = adapter
            sp_select.onItemSelectedListener = this

            et_amount = content.findViewById(R.id.dia_itemqty_amount)
            et_amount.addTextChangedListener(this)

            builder.setView(content)

            val title = getString(R.string.dialog_item_qty_title)

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
     * implementation of the [AdapterView.OnItemSelectedListener]
     *
     */
    override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
        when (pos) {
            0 -> action = "buy"
            1 -> action = "take"
            2 -> action = "split"
            3 -> action = "part"
            4 -> action = "join"
        }

        if (pos >= 3) {
            et_amount.setText("")
            et_amount.setHint(R.string.dialog_item_qty_all)
            et_amount.isEnabled = false
        } else {
            et_amount.setHint(R.string.dialog_item_qty_amount)
            et_amount.isEnabled = true
        }

        if (action == "split") {
            if (qty > cur_qty - 1) {
                et_amount.setTextColor(resources.getColor(R.color.Red))
            } else {
                et_amount.setTextColor(resources.getColor(R.color.White))
            }
        } else {
            et_amount.setTextColor(resources.getColor(R.color.White))
        }

        Log.d("info", "Auswahl: $pos")
    }
    override fun onNothingSelected(parent: AdapterView<*>) {}


    /**
     * Implementation of the [TextWatcher]
     * is used directly within the class, as we only have one
     * [EditText] to track
     */
    override fun afterTextChanged(text: Editable?) {
        var s = text.toString()
        if (s.isBlank()) s = "0"
        qty = s.toInt()

        if (action == "split") {
            if (qty > cur_qty - 1) {
                et_amount.setTextColor(resources.getColor(R.color.Red))
            } else {
                et_amount.setTextColor(resources.getColor(R.color.White))
            }
        }
    }
    // necessary implementations for TextWatcher
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}


}

