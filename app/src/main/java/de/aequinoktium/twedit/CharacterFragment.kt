package de.aequinoktium.twedit

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
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
class CharacterFragment: Fragment(),
    EditAttribDialog.EditAttribDialogListener,
    EditDamageDialog.DamageDialogListener
{
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0
    private lateinit var lp_bar: VitalAttribView
    private lateinit var ep_bar: VitalAttribView
    private lateinit var mp_bar: VitalAttribView



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
        val attrib_list = arrayOf("phy", "men", "soz", "nk", "fk", "lp", "ep", "mp")
        for (a in attrib_list) {
            var view_id = when {
                a == "phy" -> R.id.cv_phy
                a == "men" -> R.id.cv_men
                a == "soz" -> R.id.cv_soz
                a == "nk" -> R.id.cv_nk
                a == "fk" -> R.id.cv_fk
                a == "lp" -> R.id.cv_lp
                a == "ep" -> R.id.cv_ep
                a == "mp" -> R.id.cv_mp
                else -> 0
            }
            val attr_view = view.findViewById<TextView>(view_id)
            val attr_value = c.attribs[a]?: 0
            attr_view.text = attr_value.toString()
            attr_view.setOnClickListener { editAttribs(a, attr_value) }
            if (a in arrayOf("lp", "ep", "mp")) {
                attr_view.setOnLongClickListener { toggleBar(a) }
            }
        }

        lp_bar = view.findViewById(R.id.cv_lp_bar)
        ep_bar = view.findViewById(R.id.cv_ep_bar)
        mp_bar = view.findViewById(R.id.cv_mp_bar)

        val bars = arrayOf(
            Pair(lp_bar, "lp"),
            Pair(ep_bar, "ep"),
            Pair(mp_bar, "mp")
        )

        for ((bar, attr) in bars) {
            bar.max_value = c.attribs[attr]!!
            bar.visibility = View.GONE
            bar.setOnClickListener { editDamage() }
        }


        // button: switch to skills
        val b_skills = view.findViewById<Button>(R.id.cv_skills)
        b_skills.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_cs)
        }

        // button: switch to traits
        val b_traits = view.findViewById<Button>(R.id.cv_traits)
        b_traits.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_ct)
        }

        // button: switch to info
        val b_info = view.findViewById<Button>(R.id.cv_info)
        b_info.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_ci)
        }

        // button: switch to inventory
        val b_inv = view.findViewById<Button>(R.id.cv_inv)
        b_inv.setOnClickListener {
            this.findNavController().navigate(R.id.action_cv_to_cinv)
        }

    }

    fun editAttribs(char_attrib: String, cur_value: Int) {
        val fm = this.parentFragmentManager
        val dialog = EditAttribDialog(char_attrib, cur_value)
        dialog.setTargetFragment(this, 300)
        dialog.show(fm, null)
    }

    fun editDamage() {
        val fm = this.parentFragmentManager
        val dialog = EditDamageDialog()
        dialog.setTargetFragment(this, 300)
        dialog.show(fm, null)
    }

    /**
     * Handles the result of a attribute modification ...
     */
    override fun onEditAttribDialogPositiveClick(dialog: EditAttribDialog) {
        val act = activity as MainActivity
        val view_id = when {
            dialog.char_attrib == "phy" -> R.id.cv_phy
            dialog.char_attrib == "men" -> R.id.cv_men
            dialog.char_attrib == "soz" -> R.id.cv_soz
            dialog.char_attrib == "nk" -> R.id.cv_nk
            dialog.char_attrib == "fk" -> R.id.cv_fk
            dialog.char_attrib == "lp" -> R.id.cv_lp
            dialog.char_attrib == "ep" -> R.id.cv_ep
            dialog.char_attrib == "mp" -> R.id.cv_mp
            else -> 0
        }
        val view: TextView = act.findViewById(view_id)
        view.text = dialog.new_value.toString()

        c.updateAttrib(dialog.char_attrib, dialog.new_value, dialog.xp_cost)
        if (dialog.char_attrib in arrayOf("lp", "ep", "mp")) {
            c.updateVital(dialog.char_attrib, dialog.new_value.toFloat())
        }
    }

    fun toggleBar(attr: String): Boolean {
        var view: VitalAttribView
        when (attr) {
            "lp" -> view = lp_bar
            "ep" -> view = ep_bar
            "mp" -> view = mp_bar
            else -> return false
        }

        if (view.visibility == View.GONE) {
            view.invalidate()
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
        return true
    }

    override fun onDamageDialogPositiveClick(dialog: EditDamageDialog) {
        Log.d("info", "damage check")
    }
}