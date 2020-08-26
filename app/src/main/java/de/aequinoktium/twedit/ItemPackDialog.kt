package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

/**
 * A DialogFrament that is used to modify a specific character information.
 * @param info_id is the database id for the information to modify
 * @param c is the [CharacterViewModel] for this app.
 * @param v is the [View] calling this dialog
 */
class ItemPackDialog(val item: Item, val c: CharacterViewModel): DialogFragment() {
    internal lateinit var listener: DialogListener
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
            findContainers()

            builder.setView(content)


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

    fun findContainers() {

        fun selector(i: Item): Int = i.weight_limit

        for (i in c.getInventory()) {
            if (i.id == item.id) continue
            if (i.weight_limit > 0) containers += i
        }
        containers.sortByDescending { selector(it) }

        for (i in containers) {
            var state = 0
            var tv = TextView(context)
            var text = ""
            if (!i.container_name.isBlank()) {
                text = i.container_name
            } else {
                text = i.name
            }

            // the remaining capacity in the container is insufficient
            if (i.weight_limit < c.getItemTotalWeight(item) + c.getItemContentWeight(i)) {
                text += " (!)"
                state = 1
            }

            // the item is just too big/heavy for this container
            if (i.weight_limit < item.weight) {
                tv.setTextColor(resources.getColor(R.color.Red))
                state = 2
            }

            tv.text = text
            tv.setOnClickListener { v -> select(v) }

            ll_container.addView(tv)
            cont_state += state
        }
    }

    fun select(view: View) {
        for (i in 0..ll_container.childCount-1) {
            var tv = ll_container.getChildAt(i)
            tv as TextView
            when (cont_state[i]) {
                0 -> tv.setTextColor(resources.getColor(R.color.LiteGrey))
                1 -> tv.setTextColor(resources.getColor(R.color.LiteGrey))
                2 -> tv.setTextColor(resources.getColor(R.color.Red))
            }
            if (tv == view) selected = i

        }
        view as TextView
        view.setTextColor(resources.getColor(R.color.Blue))
    }

}

