package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament to load a clip with matching bullets
 */
class ItemLoadClipDialog(val item: Item): DialogFragment() {
    internal lateinit var listener: DialogListener
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
            val content: View = inflater.inflate(R.layout.dialog_item_load, null)
            val container = content.findViewById<LinearLayout>(R.id.dia_load_container)
            showAmmo(container)

            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun showAmmo(ll: LinearLayout) {
        Log.d("info", "TEST")
        val ammo = findAmmo()
        for (i in 0..2) {
            for (a in ammo[i]) {
                val tv = TextView(context)
                val text = "${a.qty}x ${a.name}"
                tv.text = text
                ll.addView(tv)
            }
        }
    }

    fun findAmmo(): Array<Array<Item>> {
        var unloaded_ammo = arrayOf<Item>()
        var loaded_ammo = arrayOf<Item>()
        var other_ammo = arrayOf<Item>()
        for (i in c.getInventory()) {
            Log.d("info", "${i.name}")
            if (i.cls == "ammo") {
                if (i.caliber.contentEquals(item.caliber)) {
                    val packed_into = c.getItemById(i.packed_into)
                    if (packed_into.cls !in arrayOf("clipsnmore", "weapon")) {
                        unloaded_ammo += i
                    } else {
                        loaded_ammo += i
                    }
                } else {
                    other_ammo += i
                }
            }
        }
        return arrayOf(unloaded_ammo, loaded_ammo, other_ammo)
    }
}

