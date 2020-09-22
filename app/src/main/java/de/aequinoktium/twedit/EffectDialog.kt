package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament that is used to modify an item damage code
 * @param item is the current item
 */
class EffectDialog(var dmg: Damage): DialogFragment() {
    internal lateinit var listener: DialogListener

    private lateinit var iv_s_dec: ImageView
    private lateinit var iv_s_inc: ImageView
    private lateinit var iv_d_dec: ImageView
    private lateinit var iv_d_inc: ImageView
    private lateinit var tv_s: TextView
    private lateinit var tv_d: TextView
    private lateinit var tv_result: TextView
    private lateinit var tv_title: TextView
    private lateinit var bt_roll: Button


    private val c: CharacterViewModel by activityViewModels()
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
            val content: View = inflater.inflate(R.layout.dialog_effect, null)
            tv_s = content.findViewById(R.id.dia_effect_s)
            tv_d = content.findViewById(R.id.dia_effect_d)
            tv_title = content.findViewById(R.id.dia_effect_title)
            tv_result = content.findViewById(R.id.dia_effect_result)
            iv_s_dec = content.findViewById(R.id.dia_effect_s_dec)
            iv_s_dec.setOnClickListener { updateDamage("s", -1) }
            iv_s_inc = content.findViewById(R.id.dia_effect_s_inc)
            iv_s_inc.setOnClickListener { updateDamage("s", 1) }
            iv_d_dec = content.findViewById(R.id.dia_effect_d_dec)
            iv_d_dec.setOnClickListener { updateDamage("d", 1) }
            iv_d_inc = content.findViewById(R.id.dia_effect_d_inc)
            iv_d_inc.setOnClickListener { updateDamage("d", -1) }
            bt_roll =  content.findViewById(R.id.dia_effect_roll)
            bt_roll.setOnClickListener { roll() }


            updateText()


            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun updateText() {
        val s = "${dmg.s}"
        val d = if (dmg.d > 0) "+${dmg.d}" else "${dmg.d}"
        tv_s.text = s
        tv_d.text = d
    }

    fun updateDamage(mode: String, delta: Int) {
        if (mode == "s") {
            dmg.s += delta
            if (dmg.s < 0) dmg.s = 0
        }
        if (mode == "d") {
            dmg.d += delta
            if (dmg.d < -9) dmg.d = -9
            if (dmg.d > 9) dmg.d = 9
        }

        updateText()
    }

    fun roll() {
        val ewt = EWT()
        val result = ewt.roll(dmg)
        tv_result.text = result[0].toString()
        tv_title.text = getString(R.string.dialog_effect)

        tv_d.visibility = View.INVISIBLE
        tv_s.visibility = View.INVISIBLE
        iv_d_dec.visibility = View.INVISIBLE
        iv_d_inc.visibility = View.INVISIBLE
        iv_s_dec.visibility = View.INVISIBLE
        iv_s_inc.visibility = View.INVISIBLE
        bt_roll.visibility = View.INVISIBLE
    }




    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}

