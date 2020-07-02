package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.database.DatabaseUtils
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity

class CharacterSelect : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(
            R.layout.fragment_character_select,
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

    fun newChar() {
        var act = activity as MainActivity

        var name = act.findViewById<EditText>(R.id.charselect_name).text.toString()
        name = name.replace("'", "\u2019")
        var data = ContentValues()
        data.put("name", name as String)
        data.put("xp_total", 330)
        act.db.insert("char_core", null, data)

        listChars()
    }

    fun openChar(id: Int) {
        Log.d("info", "Tried to open char: $id")
        var act = activity as MainActivity

        var fm = act.supportFragmentManager
        fm
            .beginTransaction()
            .replace(R.id.main_frame, CharAttribs(id))
            .commit()

    }


}