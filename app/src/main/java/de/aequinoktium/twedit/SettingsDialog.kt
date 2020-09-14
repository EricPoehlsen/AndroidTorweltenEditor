package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

/**
 * A DialogFrament that is used to change inventory settings
 */
class SettingsDialog(val edit_settings: Array<String>):
        DialogFragment(),
        CompoundButton.OnCheckedChangeListener
{

    internal val settings: SettingsViewModel by activityViewModels()
    internal lateinit var listener: DialogListener
    private lateinit var container: LinearLayout
    var values = mutableMapOf<Int, Any>()
    var fields = mutableListOf<View>()



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
            val content: View = inflater.inflate(R.layout.dialog_inventory_settings, null)
            if (content is LinearLayout) {
                container = content
                displaySettings()
            }

            builder.setView(content)

            val title = getString(R.string.dialog_invset_title)

            builder.setTitle(title)

            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    fun displaySettings(){
        var i = 0
        for (setting in edit_settings) {
            val data = setting.split(":")
            val R_id = resources.getIdentifier(
                "setting_" + data[0].replace(".", "_"),
                "string",
                "de.aequinoktium.twedit"
            )
            val text = getString(R_id)
            val type = data[1]

            if (type == "Boolean") {
                values[i] = settings.getBoolean(data[0])
                val view = CheckBox(context)
                view.isChecked = values[i] as Boolean
                view.setOnCheckedChangeListener(this)
                view.setText(text)
                container.addView(view)
                fields.add(view)
            } else if (type == "Int") {
                values[i] = settings.getInt(data[0])
                val line = LinearLayout(context)
                val tv = TextView(context)
                tv.text = text
                val et = EditText(context)
                et.setText(values[i].toString())
                et.addTextChangedListener(TextChanged(et,this))
                et.setInputType(InputType.TYPE_CLASS_NUMBER)
                line.addView(tv)
                line.addView(et)
                container.addView(line)
                fields.add(et)
            }


            i++
        }
    }
    override fun onCheckedChanged(button: CompoundButton?, state: Boolean) {
        if (button is CheckBox) {
            var i = 0
            for (view in fields) {
                if (view == button) {
                    values[i] = button.isChecked
                    Log.d("info", "$i - ${button.isChecked}")
                }
                i++
            }
        }
    }


    class TextChanged(val et: EditText, val dia: SettingsDialog): TextWatcher{
        override fun afterTextChanged(p0: Editable?) {
            val format_number = InputType.TYPE_CLASS_NUMBER
            val format_text = InputType.TYPE_CLASS_TEXT

            var i = 0
            for (view in dia.fields) {
                if (et == view) {
                    val text = et.text.toString()
                    if (text.isEmpty()) {
                        if (et.inputType == format_number) {
                            dia.values[i] = 0
                        } else {
                            dia.values[i] = "0"
                        }

                    } else {
                        if (et.inputType == format_number) {
                            dia.values[i] = text.toInt()
                        } else {
                            dia.values[i] = text
                        }
                    }
                }
                i++
            }
        }

        // unused but needed for implementation
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
}

