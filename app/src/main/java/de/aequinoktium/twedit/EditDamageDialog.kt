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
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.lang.Math.floor

class EditDamageDialog(val attr: String): DialogFragment() {
    var delta: Float = 0f
    var action: Int = -1
    private var dec = ""
    private var inc = ""

    private val ewt = EWT()
    private var ewt_dice = 1
    private var ewt_col = 0

    internal lateinit var listener: DamageDialogListener
    private lateinit var sw_dmg: Switch
    private lateinit var tv_val: TextView
    private lateinit var bt_dec: Button
    private lateinit var bt_inc: Button
    private lateinit var gl_dice: GridLayout
    private lateinit var bt_roll: Button
    private lateinit var et_dice: EditText
    private lateinit var et_col: EditText


    private val dice_ids = arrayOf(
        R.drawable.d12_1,
        R.drawable.d12_2,
        R.drawable.d12_3,
        R.drawable.d12_4,
        R.drawable.d12_5,
        R.drawable.d12_6,
        R.drawable.d12_7,
        R.drawable.d12_8,
        R.drawable.d12_9,
        R.drawable.d12_10,
        R.drawable.d12_11,
        R.drawable.d12_12
    )



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
            bt_roll = content.findViewById(R.id.dia_dmg_roll)
            bt_roll.setOnClickListener { rollDice() }
            gl_dice = content.findViewById(R.id.dia_dmg_dice_container)
            gl_dice.columnCount = 4


            et_dice = content.findViewById(R.id.dia_dmg_dice)
            et_dice.addTextChangedListener(TextChanged(et_dice, this))
            et_col = content.findViewById(R.id.dia_dmg_col)
            et_col.addTextChangedListener(TextChanged(et_col, this))

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

    fun rollDice() {
        gl_dice.removeAllViews()
        gl_dice.columnCount = 4
        val roll = ewt.roll(ewt_dice, ewt_col)

        tv_val.setText(roll[0].toString())
        delta = roll[0] as Float
        var size = 2
        val r = roll[1] as Array<Int>
        if (r.size == 3) {
            size = 3
        } else if (r.size in 4..8) {
            size = 4
        } else if (r.size in 9..20) {
            size = 5
            gl_dice.columnCount = 5
        } else if (r.size > 20) {
            size = 6
            gl_dice.columnCount = 6
        }


        for (d in r) {
            val iv_dice = ImageView(context)
            val lp = LinearLayout.LayoutParams(gl_dice.width/size, gl_dice.width/size)
            iv_dice.layoutParams = lp
            iv_dice.setImageResource(dice_ids[d])
            gl_dice.addView(iv_dice)
        }
    }


    /**
     * A [TextWatcher] implementation to make sure that the entries for the
     * EWT roll are within the bounds of the table.
     */
    class TextChanged(val et:EditText, val d: EditDamageDialog): TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            when (et.id) {
                R.id.dia_dmg_dice -> {
                    if (et.text.toString().matches("\\d+".toRegex())) {
                        val n = et.text.toString().toInt()
                        d.ewt_dice = n
                    } else {
                        d.ewt_dice = 1
                    }
                }
                R.id.dia_dmg_col -> {
                    if (et.text.toString().matches("-?\\d+".toRegex())) {
                        val n = et.text.toString().toInt()
                        if (n in -7..7) {
                            d.ewt_col = n
                            et.setTextColor(d.resources.getColor(R.color.White))
                            d.bt_roll.isEnabled = true
                        } else {
                            et.setTextColor(d.resources.getColor(R.color.Red))
                            d.bt_roll.isEnabled = false
                        }
                    } else {
                        d.ewt_col = 0
                    }
                }
            }
        }

        // needed for implementation (not used)
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

}

