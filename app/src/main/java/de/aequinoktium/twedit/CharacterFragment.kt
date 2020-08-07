package de.aequinoktium.twedit

import android.content.ContentValues
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

/**
 * The main character view.
 * primary character selector is the char_id
 */
class CharacterFragment: Fragment(), EditAttribDialog.EditAttribDialogListener {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        char_id = c.char_id
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_character, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var act = activity as MainActivity

        var tb = act.supportActionBar
        tb?.title = c.name

        // initializes attribute buttons
        val attrib_list = arrayOf("phy", "men", "soz", "nk", "fk")
        for (a in attrib_list) {
            var view_id = when {
                a == "phy" -> R.id.cv_phy
                a == "men" -> R.id.cv_men
                a == "soz" -> R.id.cv_soz
                a == "nk" -> R.id.cv_nk
                a == "fk" -> R.id.cv_fk
                else -> 0
            }
            val attr_view = act.findViewById<TextView>(view_id)
            val attr_value = c.attribs[a]?: 0
            attr_view.text = attr_value.toString()
            attr_view.setOnClickListener { editAttribs(a, attr_value) }
        }

        // button: switch to skills
        val b_skills = act.findViewById<Button>(R.id.cv_skills)
        b_skills.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_cs)
        }

        // button: switch to traits
        val b_traits = act.findViewById<Button>(R.id.cv_traits)
        b_traits.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_ct)
        }

        // button: switch to info
        val b_info = act.findViewById<Button>(R.id.cv_info)
        b_info.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_ci)
        }

        // button: switch to inventory
        val b_inv = act.findViewById<Button>(R.id.cv_inv)
        b_inv.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_cinv)
        }

    }

    fun editAttribs(char_attrib: String, cur_value: Int) {
        val act = activity as MainActivity
        val fm = this.parentFragmentManager
        val dialog = EditAttribDialog(char_attrib, cur_value)
        dialog.setTargetFragment(this, 300)
        dialog.show(fm, null)
    }

    /**
     * Handles the result of a attribute modification ...
     */
    override fun onEditAttribDialogPositiveClick(dialog: EditAttribDialog) {
        var act = activity as MainActivity
        var view_id = when {
            dialog.char_attrib == "phy" -> R.id.cv_phy
            dialog.char_attrib == "men" -> R.id.cv_men
            dialog.char_attrib == "soz" -> R.id.cv_soz
            dialog.char_attrib == "nk" -> R.id.cv_nk
            dialog.char_attrib == "fk" -> R.id.cv_fk
            else -> 0
        }
        var view: TextView = act.findViewById(view_id)
        view.text = dialog.new_value.toString()

        var data = ContentValues()
        data.put(dialog.char_attrib, dialog.new_value)
        c.db.update("char_core", data, "id = $char_id", null)
        var sql = "UPDATE char_core SET xp_used = xp_used + " +
                   dialog.xp_cost.toString() +
                   " WHERE id = " + char_id.toString()
        c.db.execSQL(sql)

    }
}