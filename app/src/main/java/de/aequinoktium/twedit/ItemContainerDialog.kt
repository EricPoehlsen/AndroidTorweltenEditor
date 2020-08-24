package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.roundToInt

/**
 * A DialogFrament that is used to modify a specific character information.
 * @param info_id is the database id for the information to modify
 * @param c is the [CharacterViewModel] for this app.
 * @param v is the [View] calling this dialog
 */
class ItemContainerDialog(
    var capacity: Int,
    var item_cont_name: String
): DialogFragment() {
    var text = ""
    private lateinit var et_name: EditText
    private lateinit var et_capacity: EditText
    private lateinit var tv_unit: TextView
    private var cur_unit = "g"

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
            val content: View = inflater.inflate(R.layout.dialog_item_container, null)

            builder.setView(content)

            et_capacity = content.findViewById(R.id.dic_capacity)
            if (capacity > 0) et_capacity.setText(capacity.toString())
            et_capacity.addTextChangedListener(TextChanged(et_capacity, this))

            et_name = content.findViewById(R.id.dic_name)
            if (item_cont_name.length > 0) et_name.setText(item_cont_name)
            et_name.addTextChangedListener(TextChanged(et_name, this))

            tv_unit = content.findViewById(R.id.dic_unit)
            tv_unit.setOnClickListener { toggleUnit() }



            val title = getString(R.string.dialog_item_cont_title)

            builder.setTitle(title)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun toggleUnit() {
        var cap = 0f
        try {
            cap = et_capacity.text.toString().toFloat()
        } catch (e: Exception) {
            Log.e("Error", e.toString())
        }

        when (cur_unit) {
            "g" -> {
                cur_unit = "kg"
                cap = cap / 1000
                et_capacity.setText(cap.toString())
                tv_unit.setText(R.string.dialog_item_cont_kgram)
            }
            "kg" -> {
                cur_unit = "g"
                cap = cap * 1000
                et_capacity.setText(cap.toString())
                tv_unit.setText(R.string.dialog_item_cont_gram)
            }
        }
    }

    /**
     * get the capacity of the container
     * @return current capacity in gram
     */
    fun updateCapacity(): Int {
        var cap = 0
        try {
            var f_cap = et_capacity.text.toString().toFloat()
            if (cur_unit == "kg") f_cap *= 1000
            cap = f_cap.roundToInt()
        } catch (e: Exception) {
            Log.e ("conversion", e.toString())
        }
        return cap
    }


    /**
     * Implementation of the [TextWatcher]
     */
    class TextChanged(var et: EditText, val d: ItemContainerDialog): TextWatcher {

        override fun afterTextChanged(text: Editable?) {
            when (et.id) {
                R.id.dic_capacity -> {
                    try {
                        var cap = et.text.toString().toFloat()
                        if (d.cur_unit == "kg") cap *= 1000
                        d.capacity = cap.roundToInt()
                    } catch (e: Exception) {
                        Log.e("conversion", e.toString())
                    }
                }
                R.id.dic_name -> {
                    d.item_cont_name = et.text.toString()
                }
            }
        }

        // necessary implementations for TextWatcher
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
}

