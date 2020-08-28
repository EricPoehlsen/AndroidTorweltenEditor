package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Math.floor

class EditDamageDialog(val attr: String): DialogFragment() {
    var delta: Float = 0f
    var action: Int = -1
    private var dec = ""
    private var inc = ""

    internal lateinit var listener: DamageDialogListener
    private lateinit var sw_dmg: Switch
    private lateinit var tv_val: TextView
    private lateinit var bt_dec: Button
    private lateinit var bt_inc: Button



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

            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_edit_damage, null)
            sw_dmg = content.findViewById(R.id.dia_dmg_switch)
            tv_val = content.findViewById(R.id.dia_dmg_val)
            bt_dec = content.findViewById(R.id.dia_dmg_dec)
            bt_dec.setOnClickListener { updateDmg(-0.5f) }
            bt_inc = content.findViewById(R.id.dia_dmg_inc)
            bt_inc.setOnClickListener { updateDmg(+0.5f) }

            builder.setView(content)

            when (attr) {
                "lp" -> {
                    dec = getString(R.string.dialog_dmg_lp_dec)
                    inc = getString(R.string.dialog_dmg_lp_inc)
                }
                "ep" -> {
                    dec = getString(R.string.dialog_dmg_ep_dec)
                    inc = getString(R.string.dialog_dmg_ep_inc)
                }
                "mp" -> {
                    dec = getString(R.string.dialog_dmg_mp_dec)
                    inc = getString(R.string.dialog_dmg_mp_inc)
                }
            }
            sw_dmg.text = inc
            sw_dmg.setOnCheckedChangeListener{ view, value -> switch(view, value)

            }

            builder.setPositiveButton(getString(R.string.dialog_ok)) { dialog, id ->
                listener.onDamageDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun updateDmg(value:Float) {
        delta += value
        if (delta <= 0f) {
            delta = 0f
            bt_dec.isEnabled = false
        } else {
            bt_dec.isEnabled = true
        }
        tv_val.text = delta.toString()

    }

    fun switch(view:CompoundButton, value: Boolean) {
        if (value) {
            view.text = dec
            action = 1
        } else {
            view.text = inc
            action = -1
        }
    }


}

