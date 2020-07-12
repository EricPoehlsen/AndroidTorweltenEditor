package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.database.getStringOrNull
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharSelectFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()

    class CharInfo {
        var id: Int = 0
        var name: String = ""
        var concept: String = ""
        var xp_free: Int = 0
        var xp_total: Int = 0
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(
            R.layout.fragment_char_select,
            container,
            false
        )

        // attach on click listeners ...
        var cs_add = root.findViewById<View>(R.id.charselect_add) as Button
        cs_add.setOnClickListener {
            this.newChar()
        }
        var cs_s = root.findViewById<View>(R.id.charselect_search) as Button
        cs_s.setOnClickListener {
            this.listChars()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var act = activity as MainActivity
        var tb = act.supportActionBar
        tb?.title = getString(R.string.charselect_title)
        listChars()
    }

    /**
     * Retrieves some character info from the database
     * @param name partial search string used to formulate the WHERE clause
     * @return an array of CharInfo
     */
    fun findCharacters(name: String): Array<CharInfo> {
        var result = emptyArray<CharInfo>()

        var sql = """
            SELECT 
                char_core.id as id, 
                char_core.name as name, 
                char_core.xp_used as xp_used, 
                char_core.xp_total as xp_total, 
                char_info.concept as concept 
            FROM 
                char_core 
            LEFT JOIN 
                char_info 
            ON 
                char_core.id = char_info.char_id 
        """.trimIndent()

        if (name.length > 0) {
            var select = name.replace("'", "\u2019")
            sql += " WHERE name LIKE '%$select%'"
        }

        sql += " ORDER BY name"

        val data: Cursor = c.db.rawQuery(sql, null)

        while (data.moveToNext()) {
            val char_info = CharInfo()
            char_info.id = data.getInt(0)
            char_info.name = data.getString(1)
            char_info.xp_free = data.getInt(3) - data.getInt(2)
            char_info.xp_total = data.getInt(3)
            char_info.concept = data.getStringOrNull(4).toString()
            result += char_info
        }
        return result
    }

    fun displayCharacters(characters: Array<CharInfo>) {
        // retrieve and clear the list+
        val act = activity as MainActivity
        var ll: LinearLayout = act.findViewById(R.id.charselect_charlist)
        ll.removeAllViews()

        for (char in characters) {
        // add entries to the list ...
            var tv = CharSelectView(context)
            tv.name.text = char.name
            tv.xp.text = char.xp_free.toString() + "/" + char.xp_total.toString()
            tv.setOnClickListener { openChar(char.id) }
            ll.addView(tv)
        }
    }

    /**
     * Listing existing characters and filter by name if given
     */
    fun listChars() {
        var act = activity as MainActivity

        // get the name as search string
        var name: String = act.findViewById<EditText>(R.id.charselect_name).text.toString()

        c.viewModelScope.launch {
            var characters = findCharacters(name)
            withContext(Dispatchers.Main) {
                displayCharacters(characters)
            }
        }
    }

    /**
     * Create a new character
     * gets value from TextEdit and creates a new Character in the database ...
     */
    fun newChar() {
        var act = activity as MainActivity


        var name = act.findViewById<EditText>(R.id.charselect_name).text.toString()
        if (name.length > 1) {
            name = name.replace("'", "\u2019")
            var data = ContentValues()
            data.put("name", name)
            data.put("xp_total", 330)
            act.db.insert("char_core", null, data)
        }

        listChars()
    }

    fun openChar(char_id: Int) {
        var fragment = this
        c.viewModelScope.launch {
            c.loadCharData(char_id)
            withContext(Dispatchers.Main) {
                fragment.findNavController().navigate(R.id.action_cs_to_ce)
            }
        }

    }
}