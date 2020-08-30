package de.aequinoktium.twedit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
 * A DialogFrament that is used to create new skills based on existing skills
 * @param skill_id is the 'parent' skill from which this skill is derived
 * @param c is the [CharacterViewModel] for this app.
 */
class NewSkillDialog(
    private var skill_id: Int,
    private var c: CharacterViewModel
): DialogFragment(), TextWatcher {
    private var base_skill_id = 0
    private var base_skill_name = ""
    private var parent_skill_id = skill_id
    private var skill_name = ""
    var parent_spec = 0

    private lateinit var select_base: TextView
    private lateinit var select_skil: TextView
    private lateinit var select_spec: TextView
    private lateinit var et_name: EditText
    var skill = SkillData()

    internal lateinit var listener: NewSkillDialogListener

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    interface NewSkillDialogListener {
        fun onNewSkillDialogPositiveClick(dialog: NewSkillDialog)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = targetFragment as NewSkillDialogListener
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
            val content: View = inflater.inflate(R.layout.dialog_new_skill, null)
            builder.setTitle(R.string.dialog_new_skill_title)
            builder.setView(content)
            select_base = content.findViewById(R.id.nsd_option1)
            select_base.setOnClickListener{setSpec(1)}
            select_skil = content.findViewById(R.id.nsd_option2)
            select_skil.setOnClickListener{setSpec(2)}
            select_spec = content.findViewById(R.id.nsd_option3)
            select_spec.setOnClickListener{setSpec(3)}
            et_name = content.findViewById(R.id.nsd_name)
            et_name.addTextChangedListener(this)

            c.viewModelScope.launch {
                loadData()
                with (Dispatchers.Main) {
                    prepareOptions()
                }
            }


            builder.setPositiveButton(R.string.dialog_ok) { dialog, id ->
                listener.onNewSkillDialogPositiveClick(this)
            }
            builder.setNegativeButton(R.string.dialog_cancel) { dialog, id ->
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cant't be null")
    }

    /**
     * retrieves the data for the parent skill and its ancestors
     */
    fun loadData()  {
        var base_id = 0
        var name = ""
        var parent_id = 0

        // retrieve data for the originating skill
        var sql = "SELECT name, spec, parent_id, is_active FROM skills WHERE id = $skill_id"
        val skill_data = c.db.rawQuery(sql, null)
        if (skill_data.moveToFirst()) {
            name = skill_data.getString(0)
            parent_spec = skill_data.getInt(1)
            parent_id = skill_data.getInt(2)
            skill.is_active = skill_data.getInt(3)
        }
        skill_data.close()
        if (parent_spec == 3) {
            sql = "SELECT id, name, parent_id, is_active FROM skills WHERE id = $parent_id"
            val parent_skill = c.db.rawQuery(sql, null)
            if (parent_skill.moveToFirst()) {
                parent_skill_id = parent_skill.getInt(0)
                skill_name = parent_skill.getString(1)
                base_id = parent_skill.getInt(2)
            }
            parent_skill.close()
            sql = "SELECT name FROM skills WHERE id = $base_id"
            val base_skill = c.db.rawQuery(sql, null)
            if (base_skill.moveToFirst()) {
                base_skill_name = base_skill.getString(0)
                base_skill_id = base_id
            }
            base_skill.close()
        } else if (parent_spec == 2) {
            skill_name = name
            sql = "SELECT name FROM skills WHERE id = $parent_id"
            val base_skill = c.db.rawQuery(sql, null)
            if (base_skill.moveToFirst()) {
                base_skill_name = base_skill.getString(0)
                base_skill_id = parent_id
            }
            base_skill.close()
        } else {
            base_skill_name = name
            base_skill_id = skill_id
        }
    }

    /**
     * prepare the selection options for the specialization level
     */
    fun prepareOptions() {
        when (parent_spec) {
            1 -> {
                select_spec.visibility = View.GONE
                select_skil.text = getString(R.string.dialog_new_skill_skill, base_skill_name)
                setSpec(2)
            }
            else -> {
                select_skil.text = getString(R.string.dialog_new_skill_skill, base_skill_name)
                select_spec.text = getString(R.string.dialog_new_skill_spec, skill_name)
                setSpec(3)
            }
        }
    }

    /**
     * The onClickListener for the specialization option selection
     */
    fun setSpec(lvl: Int) {
        val deselect = ContextCompat.getColor(select_base.context, R.color.Grey)
        val select = ContextCompat.getColor(select_base.context, R.color.Blue)
        skill.spec = lvl
        if (lvl == 1) {
            select_base.setTextColor(select)
            select_skil.setTextColor(deselect)
            select_spec.setTextColor(deselect)
            skill.parent = 0
        }
        if (lvl == 2) {
            select_base.setTextColor(deselect)
            select_skil.setTextColor(select)
            select_spec.setTextColor(deselect)
            skill.parent = base_skill_id
        }
        if (lvl == 3) {
            select_base.setTextColor(deselect)
            select_skil.setTextColor(deselect)
            select_spec.setTextColor(select)
            skill.parent = parent_skill_id
        }
    }


    class SkillData{
        var name = ""
        var parent = 0
        var spec = 0
        var is_active = 1
    }

    /**
     * Implementation of the [TextWatcher]
     * Writes the current value into the [skill] class
     */
    override fun afterTextChanged(text: Editable?) {
        skill.name = text.toString()
    }

    // necessary implementations for TextWatcher
    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

}

