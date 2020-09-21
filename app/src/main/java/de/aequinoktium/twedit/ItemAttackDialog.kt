package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import java.lang.Exception
import java.util.regex.Pattern
import kotlin.math.roundToInt

/**
 * A DialogFrament that is used to modify an item damage code
 * @param item is the current item
 */
class ItemAttackDialog(val item: Item): DialogFragment() {
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
            val content: View = inflater.inflate(R.layout.dialog_item_attack, null)

            displaySkill(content)

            builder.setView(content)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun displaySkill(parent: View) {
        val tv = parent.findViewById<TextView>(R.id.dia_item_attack_skill)
        var skills = arrayOf(
            CharacterViewModel.Skill().apply {
                id=0
                name = getString(R.string.dialog_item_skill_none)
            }
        ) + c.char_skills
        for (s in skills) {
            if (item.skill == s.id) {
                tv.text = s.name
            }
        }
    }
}

