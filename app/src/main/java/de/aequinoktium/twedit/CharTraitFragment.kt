package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Displays the character traits and allows to modify them
 */
class CharTraitFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_trait, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity

        // switch to trait selector fragment
        val b_add_skills = act.findViewById<Button>(R.id.chartraits_add)
        b_add_skills.setOnClickListener {
            this.findNavController().navigate(R.id.action_ct_to_ts)
        }

        c.viewModelScope.launch {
            var traits = loadTraits()
            withContext(Dispatchers.Main) {
                displayTraits(traits)
            }
        }

    }

    private fun loadTraits(): Array<CharTrait> {
        var result = arrayOf<CharTrait>()

        var sql = "SELECT * FROM char_traits WHERE char_id = ${c.char_id}"
        var trait_data = c.db.rawQuery(sql, null)

        while (trait_data.moveToNext()) {
            var trait = CharTrait()
            trait.id = trait_data.getInt(trait_data.getColumnIndex("id"))
            trait.trait_id = trait_data.getInt(trait_data.getColumnIndex("trait_id"))
            trait.rank = trait_data.getInt(trait_data.getColumnIndex("rank"))
            trait.variants = trait_data.getString(trait_data.getColumnIndex("variants"))
            trait.xp_cost = trait_data.getInt(trait_data.getColumnIndex("xp_cost"))
            trait.name = trait_data.getString(trait_data.getColumnIndex("name"))
            trait.txt = trait_data.getString(trait_data.getColumnIndex("txt"))
            trait.reduced = trait_data.getInt(trait_data.getColumnIndex("is_reduced"))
            result += trait
        }
        trait_data.close()
        return result
    }

    private fun displayTraits(traits: Array<CharTrait>) {
        var act = activity as MainActivity
        val layout = act.findViewById<LinearLayout>(R.id.chartraits_layout)
        for (trait in traits) {
            var tv = CharTraitView(context)
            tv.c = c
            tv.char_trait = trait
            tv.setName(trait.name)
            tv.setXp(trait.xp_cost)
            tv.setTraitId(trait.trait_id)
            tv.setVariants(trait.variants)
            tv.showRank(trait.rank)
            tv.setOnLongClickListener { view -> editTrait(view)}
            layout.addView(tv)
        }
    }

    fun editTrait(v: View): Boolean {
        var attrib = v as CharTraitView
        // switch to trait selector fragment
        c.edit_trait = v.getCharTraitId()
        this.findNavController().navigate(R.id.action_ct_to_cet)
        return true
    }
}