package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
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
class ItemDamageDialog(
    var s: Int = 0,
    var d: Int = 0,
    var e: String = ""): DialogFragment()
{
    internal lateinit var listener: DialogListener
    private lateinit var et_s: EditText
    private lateinit var et_d: EditText
    private lateinit var et_e: EditText
    lateinit var cb_mod: CheckBox



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
            val content: View = inflater.inflate(R.layout.dialog_item_dmg, null)

            et_s = content.findViewById(R.id.dia_itemdmg_s)
            et_s.addTextChangedListener(TextChanged(et_s, this))
            et_d = content.findViewById(R.id.dia_itemdmg_d)
            et_d.addTextChangedListener(TextChanged(et_d, this))
            et_e = content.findViewById(R.id.dia_itemdmg_e)
            et_e.addTextChangedListener(TextChanged(et_e, this))
            cb_mod = content.findViewById(R.id.dia_itemdmg_mod)

            builder.setView(content)

            val title = getString(R.string.dialog_item_dmg_title)

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
    class TextChanged(var et: EditText, val dialog: ItemDamageDialog): TextWatcher {

        override fun afterTextChanged(text: Editable?) {
            when (et.id) {
                R.id.dia_itemdmg_s -> {
                    val s = et.text.toString()
                    if (s.matches("-?\\d+".toRegex())) {
                        val d = s.toInt()
                        if (d in -12..12) {
                            et.setTextColor(et.resources.getColor(R.color.White))
                        } else {
                            et.setTextColor(et.resources.getColor(R.color.Red))
                        }
                        dialog.s = d
                    }
                }
                R.id.dia_itemdmg_d -> {
                    val s = et.text.toString()
                    if (s.matches("-?\\d+".toRegex())) {
                        val d = s.toInt()
                        if (d in -7..7) {
                            et.setTextColor(et.resources.getColor(R.color.White))
                        } else {
                            et.setTextColor(et.resources.getColor(R.color.Red))
                        }
                        dialog.d = d
                    }
                }
                R.id.dia_itemdmg_e -> {
                    val s = et.text.toString()
                    if (s.matches("[pPeEmM]".toRegex())) {
                        et.setTextColor(et.resources.getColor(R.color.White))
                    } else {
                        et.setTextColor(et.resources.getColor(R.color.Red))
                    }
                    dialog.e = s
                }
            }
        }

        // necessary implementations for TextWatcher
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }


}

