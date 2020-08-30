package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
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
    private val d: DataViewModel by activityViewModels()

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


    fun displayCharacters(characters: Array<DataViewModel.CharInfo>) {
        // retrieve and clear the list+
        val act = activity as MainActivity
        val ll: LinearLayout = act.findViewById(R.id.charselect_charlist)
        ll.removeAllViews()

        for (char in characters) {
        // add entries to the list ...
            val tv = CharSelectView(context)
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
        val name: String = act.findViewById<EditText>(R.id.charselect_name).text.toString()

        c.viewModelScope.launch(Dispatchers.IO) {
            var characters = d.findCharacters(name)
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