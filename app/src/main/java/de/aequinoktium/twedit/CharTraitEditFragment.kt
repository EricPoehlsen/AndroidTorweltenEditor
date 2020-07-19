package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CharTraitEditFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private var default_data = TraitData()
    private var trait_data = CharTrait()
    private var trait_vars = arrayOf<TraitVariant>()


    private lateinit var tv_name: TextView
    private lateinit var tv_xp: TextView
    private lateinit var tv_txt: TextView


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var act = activity as MainActivity
        tv_name = act.findViewById<TextView>(R.id.traitedit_name)
        tv_xp = act.findViewById<TextView>(R.id.traitedit_xp)
        tv_txt = act.findViewById<TextView>(R.id.traitedit_text)
        var b_ok = act.findViewById<Button>(R.id.traitedit_ok)
        b_ok.setOnClickListener {saveTrait()}
        var b_cancel = act.findViewById<Button>(R.id.traitedit_cancel)
        b_cancel.setOnClickListener{backToCharTraits()}
        c.viewModelScope.launch {
            loadData()
            withContext(Dispatchers.Main) {
                displayTrait()
            }
        }
    }

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
        }

        text = text.replace("<br/>", "\n")
        text = text.replace("<", "[")
        text = text.replace(">", "]")
        tv_txt.text = text

        tv_xp.text = trait_data.xp_cost.toString()
    }


    fun loadData() {
        var sql = """
            SELECT
                name,
                trait_id,
                txt,
                xp_cost, 
                rank
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
    }

    fun updateTrait() {
        trait_data.name = tv_name.text.toString()
        trait_data.xp_cost = tv_xp.text.toString().toInt()
        var txt = tv_txt.text.toString()
        txt = txt.replace("[", "<")
        txt = txt.replace("]", ">")
        txt = txt.replace("\n", "<br/>")

        trait_data.txt = txt
    }


    fun saveTrait() {
        updateTrait()


        var sql = """
            UPDATE 
                char_traits
            SET 
                name = '${trait_data.name}',
                txt = '${trait_data.txt}',
                xp_cost = ${trait_data.xp_cost},
                rank = ${trait_data.rank}
            WHERE
                id = ${trait_data.id}
        """.trimIndent()
        c.db.execSQL(sql)

        var delta_xp = trait_data.xp_cost - trait_data.xp_old
        Log.d("info", "oid_xp: ${trait_data.xp_old}")
        sql = """
            UPDATE
                char_core
            SET
                xp_used = xp_used + $delta_xp
            WHERE
                id = ${c.char_id}
        """.trimIndent()

        Log.d("info", "SQL: $sql")
        c.db.execSQL(sql)

        backToCharTraits()




    }

    fun backToCharTraits() {
        this.findNavController().popBackStack()
    }

}