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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament that to pack an [Item] into another [Item]
 * @param info_id is the database id for the information to modify
 * @param c is the [CharacterViewModel] for this app.
 * @param v is the [View] calling this dialog
 */
class ItemPackDialog(val item: Item): DialogFragment() {
    internal lateinit var listener: DialogListener
    val settings: SettingsViewModel by activityViewModels()
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var ll_container: LinearLayout
    internal var cont_state = arrayOf<Int>()
    var containers = arrayOf<Item>()
    var selected = -1


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
            val content: View = inflater.inflate(R.layout.dialog_item_pack, null)

            ll_container = content.findViewById(R.id.dia_itempack_container)
            val tv_title = content.findViewById<TextView>(R.id.dia_itempack_title)
            val title = getString(R.string.dialog_item_pack_title, item.name)
            tv_title.text = title
            findContainers()

            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun findContainers() {
        fun selector(i: Item): Int = i.weight_limit

        for (i in c.getInventory()) {
            if (i.id == item.id) continue
            if (i.weight_limit > 0) containers += i
        }
        containers.sortByDescending { selector(it) }

        for (i in containers) {
            var use_container = true
            var text = ""
            if (!i.container_name.isBlank()) {
                text = i.container_name
            } else {
                text = i.name
            }

            // container has caliber check the caliber against the items caliber
            if (!i.caliber[0].isEmpty() && !i.caliber[1].isEmpty()) {
                if (!i.caliber.contentEquals(item.caliber)) {
                    text += " !"
                    if (settings.getBoolean("inventory.check_caliber")) use_container = false
                }
            }

            // check if the remaining capacity of the container is sufficient
            if (i.weight_limit < c.getItemTotalWeight(item) + c.getItemContentWeight(i)) {
                if (settings.getBoolean("inventory.check_weight_limit")) use_container = false
                text += " !"
            }

            val tv = TextView(context)
            tv.text = text
            tv.setOnClickListener { v -> select(v) }

            if (use_container) ll_container.addView(tv)
        }
    }

    fun select(view: View) {
        val grey = ContextCompat.getColor(requireContext(), R.color.Grey)
        val blue = ContextCompat.getColor(requireContext(), R.color.Blue)

        for (i in 0..ll_container.childCount-1) {
            var tv = ll_container.getChildAt(i)
            tv as TextView
            tv.setTextColor(grey)
            if (tv == view) selected = i

        }
        view as TextView
        view.setTextColor(blue)
    }

}

