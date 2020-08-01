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

/**
 * A DialogFrament that is used to modify a specific character information.
 * @param info_id is the database id for the information to modify
 * @param c is the [CharacterViewModel] for this app.
 * @param v is the [View] calling this dialog
 */
class EditInfoDialog(
    var info_id: Int,
    var dataset: String,
    private var c: CharacterViewModel,
    var v: View
): DialogFragment(), TextWatcher {
    var text = ""
    private lateinit var et_text: EditText


    internal lateinit var listener: EditInfoDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface EditInfoDialogListener {
        fun onEditInfoDialogPositiveClick(dialog: EditInfoDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as EditInfoDialogListener
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

            val act = it as MainActivity

            val inflater: LayoutInflater = this.layoutInflater
            val content: View = inflater.inflate(R.layout.dialog_edit_info, null)

            builder.setView(content)

            et_text = content.findViewById(R.id.dei_text)
            et_text.setText(loadText())
            et_text.addTextChangedListener(this)
            val name = loadName()
            val title = getString(R.string.dialog_edit_info_title, name)

            builder.setTitle(title)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onEditInfoDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    /**
     * get text from [CharacterViewModel.info]
     * parse from HTML to Markdown.
     * @return the text
     */
    fun loadText(): String {
        var txt = ""
        for (i in c.info[dataset]!!) {
            if (i.info_id == info_id) {
                txt = i.txt
                txt = txt.replace("<b>", "**")
                txt = txt.replace("</b>", "**")
                txt = txt.replace("<i>", "//")
                txt = txt.replace("</i>", "//")
                txt = txt.replace("<u>", "__")
                txt = txt.replace("</u>", "__")
                txt = txt.replace("<br/>", "\n")

            }
        }
        if (info_id == -1) txt = c.name
        return txt
    }

    fun loadName(): String {
        var name = ""
        for (i in c.info[dataset]!!) {
            if (i.info_id == info_id) {
                name = i.name
                if (dataset == "core") {
                    when (name) {
                        "species" -> name = getString(R.string.ci_species)
                        "concept" -> name = getString(R.string.ci_concept)
                        "homeworld" -> name = getString(R.string.ci_homeworld)
                        "culture" -> name = getString(R.string.ci_culture)
                        "notes" -> name = getString(R.string.ci_notes)
                    }
                } else if (dataset == "desc"){
                    when (name) {
                        "age" -> name = getString(R.string.ci_age)
                        "size" -> name = getString(R.string.ci_size)
                        "weight" -> name = getString(R.string.ci_weight)
                        "sex" -> name = getString(R.string.ci_sex)
                        "build" -> name = getString(R.string.ci_build)
                        "eyecolor" -> name = getString(R.string.ci_eye_color)
                        "color1" -> name = getString(R.string.ci_color1)
                        "color2" -> name = getString(R.string.ci_color2)
                        "desc" -> name = getString(R.string.ci_desc)
                    }
                }
            }
        }
        if (info_id == -1) {
            name = getString(R.string.ci_name)
        }


        return name
    }

    /**
     * Implementation of the [TextWatcher]
     * Writes the current value into the [text] variable
     */
    override fun afterTextChanged(text: Editable?) {
        this.text = text.toString()
        Log.d("info", this.text)
    }

    // necessary implementations for TextWatcher
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

}

