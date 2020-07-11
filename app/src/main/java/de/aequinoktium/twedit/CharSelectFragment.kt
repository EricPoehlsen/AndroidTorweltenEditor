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
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController


class CharSelectFragment : Fragment() {
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
     * Listing existing characters and filter by name if given
     */
    fun listChars() {
        var act = activity as MainActivity

        // get the name as search string
        var name: String = act.findViewById<EditText>(R.id.charselect_name).text.toString()
        var selection: String?
        if (name.length > 0) {
            name = name.replace("'", "\u2019")
            Log.d("info", name)
            selection = "name LIKE '%$name%'"
        } else {
            selection = null
        }

        // query the database
        var result: Cursor = act.db.query(
            "char_core",
            arrayOf("id", "name", "xp_total"),
            selection,
            null,
            null,
            null,
            "name"
        )

        // retrieve and clear the list
        var ll: LinearLayout = act.findViewById(R.id.charselect_charlist)
        ll.removeAllViews()

        // add entries to the list ...
        result.moveToFirst()
        repeat(result.count) {
            var tv = TextView(context)
            tv.text = result.getString(1)
            var char_id = result.getInt(0)
            tv.setOnClickListener { openChar(char_id) }
            ll.addView(tv)
            result.moveToNext()
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
        val bundle: Bundle = bundleOf("char_id" to char_id)
        this.findNavController().navigate(R.id.action_cs_to_ce, bundle)
    }
}