package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharSelectFragment :
        Fragment(),
        CharDeleteDialog.DialogListener,
        SettingsDialog.DialogListener
{
    private val c: CharacterViewModel by activityViewModels()
    private val d: DataViewModel by activityViewModels()
    private val settings: SettingsViewModel by activityViewModels()

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
        val act = activity as MainActivity
        val tb = act.supportActionBar
        tb?.title = getString(R.string.charselect_title)

        // attach on click listeners ...
        val bt_add = view.findViewById<Button>(R.id.charselect_add)
        bt_add.setOnClickListener { newChar() }
        val bt_search = view.findViewById<Button>(R.id.charselect_search)
        bt_search.setOnClickListener { listChars() }
        val iv_settings = view.findViewById<ImageView>(R.id.charselect_settings)
        iv_settings.setOnClickListener { editSettings() }


        listChars()
    }


    fun displayCharacters(characters: Array<DataViewModel.CharInfo>, deleted: Boolean = false) {
        // retrieve and clear the list+
        val act = activity as MainActivity
        val ll: LinearLayout = act.findViewById(R.id.charselect_charlist)
        ll.removeAllViews()

        for (char in characters) {
        // add entries to the list ...
            if (
                !deleted
                && char.name.startsWith("#")
                && char.deleted
            ) continue

            val csv = CharSelectView(context)
            csv.name.text = char.name
            csv.xp.text = getString(R.string.charselect_xp, char.xp_free, char.xp_total)
            csv.concept.text = char.concept
            csv.setOnClickListener { openChar(char.id) }
            csv.delete.setOnLongClickListener{ deleteChar(char) }
            ll.addView(csv)
        }
    }

    /**
     * Listing existing characters and filter by name if given
     */
    fun listChars() {
        // get the name as search string
        val act = context as MainActivity
        val name: String = act.findViewById<EditText>(R.id.charselect_name).text.toString()
        val deleted = name.startsWith("#")

        c.viewModelScope.launch(Dispatchers.IO) {
            val characters = d.findCharacters(name)
            withContext(Dispatchers.Main) {
                displayCharacters(characters, deleted)
            }
        }
    }

    /**
     * Create a new character
     * gets value from TextEdit and creates a new Character in the database ...
     */
    fun newChar() {
        val act = context as MainActivity
        val name = act.findViewById<EditText>(R.id.charselect_name).text.toString()
        if (name.length > 1) {
            d.viewModelScope.launch(Dispatchers.IO) {
                d.addCharacter(name,settings.getInt("core.initial_xp"))
                withContext(Dispatchers.Main) {
                    listChars()
                }
            }
        }
    }

    fun openChar(char_id: Int) {
        val fragment = this
        c.viewModelScope.launch(Dispatchers.IO) {
            c.loadCharData(char_id)
            withContext(Dispatchers.Main) {
                fragment.findNavController().navigate(R.id.action_cs_to_ce)
            }
        }
    }

    fun deleteChar(char: DataViewModel.CharInfo): Boolean {
        val fm = this.parentFragmentManager
        val dialog = CharDeleteDialog(char)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
        return true
    }

    fun editSettings() {
        val fm = this.parentFragmentManager
        val dialog = SettingsDialog(arrayOf("core.initial_xp:Int"))
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is CharDeleteDialog) {
            d.viewModelScope.launch(Dispatchers.IO) {
                d.deleteCharacter(dialog.char)
                withContext(Dispatchers.Main) {
                    listChars()
                }
            }
        } else if (dialog is SettingsDialog) {
            val new_xp = dialog.values[0] as Int
            settings.update("core.initial_xp", new_xp)
        }
    }
}