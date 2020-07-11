package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import android.view.Display
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

/**
 * The main character view.
 * primary character selector is the char_id
 */
class CharacterFragment: Fragment(), EditAttribDialog.EditAttribDialogListener {
    var char_id = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            char_id = it.getInt("char_id")
        }

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


        var data: Cursor = act.db.rawQuery(
            "SELECT * FROM char_core WHERE id = $char_id",
            null
        )

        data.moveToFirst()

        var tb = act.supportActionBar
        tb?.title = data.getString(data.getColumnIndex("name"))


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
            val attr_value = data.getInt(data.getColumnIndex(a))
            attr_view.text = attr_value.toString()
            attr_view.setOnClickListener { editAttribs(a, attr_value) }
        }

        data.close()

        var b_skills = act.findViewById<Button>(R.id.cv_skills)
        b_skills.setOnClickListener {
            val bundle: Bundle = bundleOf("char_id" to char_id)
            this.findNavController().navigate(R.id.action_cv_to_cs, bundle)
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
        act.db.update("char_core", data, "id = $char_id", null)
        var sql = "UPDATE char_core SET xp_used = xp_used + " +
                   dialog.xp_cost.toString() +
                   " WHERE id = " + char_id.toString()
        act.db.execSQL(sql)

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param char_id the character id in the database.
         * @return A new instance of fragment CharEditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(char_id: Int) =
            CharacterFragment().apply {
                arguments = Bundle().apply {
                    putInt("char_id", char_id)
                }
            }
    }
}