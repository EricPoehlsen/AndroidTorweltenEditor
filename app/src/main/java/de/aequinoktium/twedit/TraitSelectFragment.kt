package de.aequinoktium.twedit

import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [TraitSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TraitSelectFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()

    class TraitData {
        var id = 0
        var name = ""
        var xp = 0
        var max_rank = 0
        var variants = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trait_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        c.viewModelScope.launch {
            var traits = findTraits()
            withContext(Dispatchers.Main) {
                displayTraits(traits)
            }
        }
    }

    fun displayTraits(traits: Array<TraitData>) {
        val act = activity as MainActivity
        val container = act.findViewById<LinearLayout>(R.id.traitselect_container)

        for (trait in traits) {
            var tv = TraitView(context)
            tv.c = c

            var lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, act.calc_dp(4), 0)

            tv.layoutParams = lp
            tv.setTraitId(trait.id)
            tv.setName(trait.name)
            tv.setComplex(trait.variants, trait.max_rank)
            tv.setXp(trait.xp)

            container.addView(tv)
        }
    }

    fun findTraits(): Array<TraitData> {
        var traits = arrayOf<TraitData>()
        var sql = """
            SELECT trait_id FROM trait_vars    
        """.trimIndent()
        var data = c.db.rawQuery(sql, null)
        var variant_traits = arrayOf<Int>()
        while (data.moveToNext()) {
            if (data.getInt(0) !in variant_traits) {
                variant_traits += data.getInt(0)
            }
        }
        data.close()

        sql = """
            SELECT
                id, 
                name,
                xp_cost, 
                max_rank
            FROM 
                traits
            ORDER BY
                grp
        """.trimIndent()
        data = c.db.rawQuery(sql, null)
        while(data.moveToNext()) {
            var trait = TraitData()
            trait.id = data.getInt(0)
            trait.name = data.getString(1)
            trait.xp = data.getInt(2)
            trait.max_rank = data.getInt(3)
            if (trait.id in variant_traits) {
                trait.variants = true
            }
            traits += trait
        }
        data.close()
        return traits
    }
}