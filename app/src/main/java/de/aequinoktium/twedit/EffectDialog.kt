package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament that is used to modify an item damage code
 * @param item is the current item
 */
class EffectDialog(val dmg: Damage): DialogFragment() {
    internal lateinit var listener: DialogListener
    private lateinit var ll: LinearLayout

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
            ll = content.findViewById(R.id.dia_item_effect)

            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun centeredText(): TextView {
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val tv = TextView(context)
        tv.layoutParams = lp
        tv.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        return tv
    }

    fun button():TextView {
        val margins = px(3).toInt()
        val padding = px(3).toInt()

        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        lp.setMargins(margins, margins, margins, margins)
        val tv = TextView(context)
        tv.minEms = 2
        tv.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        tv.setTextColor(Color.WHITE)
        tv.setPadding(padding, padding, padding, padding)
        tv.setBackgroundColor(Color.GRAY)
        tv.layoutParams = lp

        return tv
    }



    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}

