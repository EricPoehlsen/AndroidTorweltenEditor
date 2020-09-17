package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament to assign ammo to clips or clips to weapons
 * @param item the [Item] to which we are assigning
 * @param cls the item class we would like to assign ("ammo" or "clipsnmore")
 */
class ItemLoadClipDialog(val item: Item, val cls:String): DialogFragment() {
    internal lateinit var listener: DialogListener
    private val c: CharacterViewModel by activityViewModels()
    private val settings: SettingsViewModel by activityViewModels()
    var selected_id = 0
    var slots = 0


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
            show(container)

            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun show(ll: LinearLayout) {
        var usable = ""
        val items = find()
        for (i in 0..2) {
            if (i == 1) usable = " (?)"
            if (i == 2) usable = " (!)"
            for (a in items[i]) {
                val tv = TextView(context)
                val text = "${a.qty}x ${a.name} $usable"
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val padding = px(6).toInt()
                tv.setPadding(padding,padding,padding,padding)
                tv.tag = a.id
                tv.text = text
                tv.setOnClickListener {v -> select(v)}
                ll.addView(tv)
            }
        }
    }

    fun find(): Array<Array<Item>> {
        var unloaded_items = arrayOf<Item>()
        var loaded_items = arrayOf<Item>()
        var other_items = arrayOf<Item>()
        for (i in c.getInventory()) {
            if (i.cls == cls) {
                if (i.caliber.contentEquals(item.caliber)) {
                    val packed_into = c.getItemById(i.packed_into)
                    if (packed_into.cls !in arrayOf("clipsnmore", "weapon")) {
                        unloaded_items += i
                    } else {
                        loaded_items += i
                    }
                } else {
                    if (!settings.getBoolean("inventory.check_caliber"))
                    other_items += i
                }
            }
        }
        return arrayOf(unloaded_items, loaded_items, other_items)
    }

    fun select(view: View) {
        val grey = ContextCompat.getColor(requireContext(), R.color.Grey)
        val blue = ContextCompat.getColor(requireContext(), R.color.Blue)
        if (view is TextView) {
            selected_id = view.tag as Int
        }
        for (tv in view.parent as LinearLayout) {
            tv as TextView
            tv.setTextColor(grey)
            if (tv == view) tv.setTextColor(blue)
        }

    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}

