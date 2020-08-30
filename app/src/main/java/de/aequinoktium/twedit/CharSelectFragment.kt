package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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

    private lateinit var bt_add: Button
    private lateinit var bt_search: Button



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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var act = activity as MainActivity
        var tb = act.supportActionBar
        tb?.title = getString(R.string.charselect_title)

        // attach on click listeners ...
        bt_add = view.findViewById<View>(R.id.charselect_add) as Button
        bt_add.setOnClickListener { v -> newChar() }
        bt_search = view.findViewById<View>(R.id.charselect_search) as Button
        bt_search.setOnClickListener { v -> listChars() }

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
                char_core.xp_total as xp_total
            FROM 
                char_core 
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
            sql = """
                SELECT 
                    txt 
                FROM 
                    char_info
                WHERE
                    char_id = ${char_info.id}
                    AND
                    name = 'concept'
            """.trimIndent()
            var concept = c.db.rawQuery(sql, null)
            if (concept.moveToFirst()) {
                char_info.concept = concept.getString(0).toString()
            }
            concept.close()
            result += char_info
        }

        data.close()
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
        // get the name as search string
        val act = context as MainActivity
        var name: String = act.findViewById<EditText>(R.id.charselect_name).text.toString()

        c.viewModelScope.launch(Dispatchers.IO) {
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
        val act = context as MainActivity
        var name = act.findViewById<EditText>(R.id.charselect_name).text.toString()
        if (name.length > 1) {
            name = name.replace("'", "\u2019")
            var data = ContentValues()
            data.put("name", name)
            data.put("xp_total", 330)
            c.db.insert("char_core", null, data)
        }

        listChars()
    }

    fun openChar(char_id: Int) {
        var fragment = this
        c.viewModelScope.launch(Dispatchers.IO) {
            c.loadCharData(char_id)
            withContext(Dispatchers.Main) {
                fragment.findNavController().navigate(R.id.action_cs_to_ce)
            }
        }

    }
}