package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CharTraitEditFragment : Fragment(),
        RemoveTraitDialog.RemoveTraitDialogListener,
        CollapseTraitDialog.CollapseTraitDialogListener{
    private val c: CharacterViewModel by activityViewModels()
    private var default_data = TraitData()
    private var trait_data = CharTrait()
    private var trait_vars = arrayOf<TraitVariant>()

    private lateinit var tv_name: TextView
    private lateinit var tv_xp: TextView
    private lateinit var tv_txt: TextView
    private lateinit var b_collapse: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_trait_edit, container, false)
    }

    /**
     * Initialize the view after it has been created ...
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var act = activity as MainActivity
        tv_name = act.findViewById<TextView>(R.id.traitedit_name)
        tv_xp = act.findViewById<TextView>(R.id.traitedit_xp)
        tv_txt = act.findViewById<TextView>(R.id.traitedit_text)

        // set onClickListeners ...
        val b_cancel = act.findViewById<Button>(R.id.traitedit_cancel)
        b_cancel.setOnClickListener{backToCharTraits()}
        val b_ok = act.findViewById<Button>(R.id.traitedit_ok)
        b_ok.setOnClickListener {saveTrait()}
        b_collapse = act.findViewById<ImageButton>(R.id.traitedit_collapse)
        b_collapse.setOnClickListener {collapseTraitDialog()}
        b_collapse.visibility = View.GONE
        val b_delete = act.findViewById<ImageButton>(R.id.traitedit_delete)
        b_delete.setOnClickListener {deleteTraitDialog()}

        // load trait data from database and display ...
        c.viewModelScope.launch {
            loadData()
            Log.d("info", "trait data reduced: ${trait_data.reduced}")
            withContext(Dispatchers.Main) {
                if (trait_data.reduced == 0 && trait_vars.size > 0){
                    b_collapse.visibility = View.VISIBLE
                }
                displayTrait()
            }
        }
    }

    /**
     * displays the character trait,
     * transforms html entities into markdown ...
     */
    fun displayTrait() {
        if (trait_data.name.length > 0) {
            tv_name.text = trait_data.name
        } else {
            tv_name.text = default_data.name
        }

        var text = ""
        if (trait_data.txt.length > 0) {
            text = trait_data.txt
        } else {
            text = default_data.txt
            trait_data.txt = default_data.txt
        }

        text = text.replace("<br/>", "\n")
        text = text.replace("<b>", "**")
        text = text.replace("</b>", "**")
        text = text.replace("<u>", "__")
        text = text.replace("</u>", "__")
        text = text.replace("<i>", "//")
        text = text.replace("</i>", "//")
        tv_txt.text = text

        tv_xp.text = trait_data.xp_cost.toString()
    }

    /**
     * retrieves the trait data from the SQLite database
     */
    fun loadData() {
        var sql = """
            SELECT
                name,
                trait_id,
                txt,
                xp_cost, 
                rank,
                variants,
                is_reduced
            FROM 
                char_traits
            WHERE
                id = ${c.edit_trait}
        """.trimIndent()
        var data = c.db.rawQuery(sql, null)
        if (data.moveToFirst()) {
            trait_data.id = c.edit_trait
            trait_data.name = data.getString(0)
            trait_data.trait_id = data.getInt(1)
            trait_data.txt = data.getString(2)
            trait_data.xp_cost = data.getInt(3)
            trait_data.xp_old = data.getInt(3)
            trait_data.rank = data.getInt(4)
            trait_data.variants = data.getString(5).replace(" ", ",")
            trait_data.reduced = data.getInt(6)
        }
        data.close()
        sql = """
            SELECT
                name,
                txt,
                max_rank
            FROM
                traits
            WHERE 
                id = ${trait_data.trait_id}
        """.trimIndent()
        data = c.db.rawQuery(sql, null)
        if (data.moveToFirst()) {
            default_data.name = data.getString(0)
            default_data.txt = data.getString(1)
        }
        data.close()

        sql = """
            SELECT
                name, 
                txt
            FROM 
                trait_vars
            WHERE
                id 
            IN 
                (${trait_data.variants}) 
        """.trimIndent()
        data = c.db.rawQuery(sql, null)
        while (data.moveToNext()) {
            var v = TraitVariant()
            v.name = data.getString(0)
            v.txt = data.getString(1)
            trait_vars += v
        }
        data.close()
    }

    /**
     * updates the trait data before it is written back to the database
     * contains a simple markdown parser to replace:
     * ** with <b>/</b>
     * // with <i>/</i>
     * __ with <u>/</u>
     * \n with <br/>
     *
     * adds a closing tag if the user did leave not close the pairs ...
     */
    fun updateTrait() {
        trait_data.name = tv_name.text.toString()
        trait_data.xp_cost = tv_xp.text.toString().toInt()
        var txt = tv_txt.text.toString()

        var b = false
        while ("**" in txt) {
            if (!b) {
                txt = txt.replaceFirst("**", "<b>")
                b = true
            } else {
                txt = txt.replaceFirst("**", "</b>")
                b = false
            }
        }
        if (b) txt += "</b>"

        var u = false
        while ("__" in txt) {
            if (!u) {
                txt = txt.replaceFirst("__", "<u>")
                u = true
            } else {
                txt = txt.replaceFirst("__", "</u>")
                u = false
            }
        }
        if (u) txt += "</u>"

        var i = false
        while ("//" in txt) {
            if (!i) {
                txt = txt.replaceFirst("//", "<i>")
                i = true
            } else {
                txt = txt.replaceFirst("//", "</i>")
                i = false
            }
        }
        if (i) txt += "</i>"

        txt = txt.replace("\n", "<br/>")

        trait_data.txt = txt

    }

    /**
     * saves the changes to the character trait to the SQLite database
     * and invokes the return to the CharTraitFragment
     */
    fun saveTrait() {

        c.viewModelScope.launch {
            updateTrait()

            var sql = """
                UPDATE 
                    char_traits
                SET 
                    name = '${trait_data.name}',
                    txt = '${trait_data.txt}',
                    xp_cost = ${trait_data.xp_cost},
                    rank = ${trait_data.rank},
                    is_reduced = ${trait_data.reduced} 
                WHERE
                    id = ${trait_data.id}
            """.trimIndent()
            c.db.execSQL(sql)

            var delta_xp = trait_data.xp_cost - trait_data.xp_old
            sql = """
                UPDATE
                    char_core
                SET
                    xp_used = xp_used + $delta_xp
                WHERE
                    id = ${c.char_id}
            """.trimIndent()

            Log.d("info", "reduced?: {${trait_data.reduced}}")
            c.db.execSQL(sql)

            withContext(Dispatchers.Main) {
                backToCharTraits()
            }
        }
    }

    /**
     * creates a DeleteTraitDialog and displays it
     */
    fun deleteTraitDialog() {
        val act = activity as MainActivity
        val fm = this.parentFragmentManager
        val dialog = RemoveTraitDialog(trait_data.name)
        dialog.setTargetFragment(this, 300)
        dialog.show(fm, null)
    }

    /**
     * creates a CollapseTraitDialog and displays it
     */
    fun collapseTraitDialog() {
        val act = activity as MainActivity
        val fm = this.parentFragmentManager
        val dialog = CollapseTraitDialog()
        dialog.setTargetFragment(this, 300)
        dialog.show(fm, null)
    }

    /**
     * remove trait from the database and correct the XP value
     * invokes back stack to return to CharTraitFragment
     */
    fun deleteTrait() {
        c.viewModelScope.launch {
            var sql = """
                DELETE FROM 
                    char_traits
                WHERE
                    id = ${trait_data.id}
            """.trimIndent()
            c.db.execSQL(sql)

            sql = """
                UPDATE
                    char_core
                SET
                    xp_used = xp_used - ${trait_data.xp_old}
                WHERE
                    id = ${c.char_id}
            """.trimIndent()

            c.db.execSQL(sql)
            withContext(Dispatchers.Main) {
                backToCharTraits()
            }
        }
    }

    fun collapseTrait() {
        for (v in trait_vars) {
            trait_data.txt = trait_data.txt + "\n${v.name}\n${v.txt}"
            trait_data.reduced = 1
            displayTrait()
            b_collapse.visibility = View.GONE

        }
    }


    fun backToCharTraits() {
        this.findNavController().popBackStack()
    }

    /**
     * Implementation of the listener for the RemoveTraitDialog
     */
    override fun onRemoveTraitDialogPositiveClick(dialog: RemoveTraitDialog) {
        deleteTrait()
    }

    /**
     * Implementation of the listener for the CollapseTraitDialog
     */
    override fun onCollapseTraitDialogPositiveClick(dialog: CollapseTraitDialog) {
        collapseTrait()
    }

}