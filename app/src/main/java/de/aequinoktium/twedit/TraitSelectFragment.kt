package de.aequinoktium.twedit

import android.database.Cursor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.marginBottom
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.findFragment
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
    private var toggle = 0
    private lateinit var ll_container: LinearLayout
    private lateinit var te_search: EditText
    private lateinit var b_toggle: Button

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
        val act = activity as MainActivity
        ll_container = act.findViewById<LinearLayout>(R.id.traitselect_container)

        c.viewModelScope.launch {
            var traits = findTraits()
            withContext(Dispatchers.Main) {
                displayTraits(traits)
            }
        }


        b_toggle = act.findViewById<Button>(R.id.traitselect_toggle)
        b_toggle.setOnClickListener{v -> toggleTraits(v)}

        te_search = act.findViewById<EditText>(R.id.traitselect_search)

        val watcher = TextChanged(te_search)
        te_search.addTextChangedListener(watcher)
    }

    fun toggleTraits(view: View){
        if (view == b_toggle) {
            toggle += 1
            if (toggle > 2) toggle = 0
        }
        var search = te_search.text.toString().toLowerCase()

        when (toggle) {
            0 -> {
                b_toggle.text = getText(R.string.ts_all)
                for (v in ll_container.children) {
                    val trait = v as TraitView
                    trait.visibility = View.VISIBLE

                    val name = trait.getName().toLowerCase()
                    if (search.length >= 1 && search !in name) {
                        trait.visibility = View.GONE
                    }
                }
            }
            1 -> {
                b_toggle.text = getText(R.string.ts_advantages)
                for (v in ll_container.children) {
                    val trait = v as TraitView
                    if (trait.getXp() > 0) {
                        trait.visibility = View.VISIBLE
                    } else {
                        trait.visibility = View.GONE
                    }

                    val name = trait.getName().toLowerCase()
                    if (search.length >= 1 && search !in name) {
                        trait.visibility = View.GONE
                    }
                }
            }
            2 -> {
                b_toggle.text = getText(R.string.ts_disadvantages)
                for (v in ll_container.children) {
                    val trait = v as TraitView
                    if (trait.getXp() < 0) {
                        trait.visibility = View.VISIBLE
                    } else {
                        trait.visibility = View.GONE
                    }

                    val name = trait.getName().toLowerCase()
                    if (search.length >= 1 && search !in name) {
                        trait.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun displayTraits(traits: Array<TraitData>) {
        val act = activity as MainActivity

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

            ll_container.addView(tv)
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
                cls, grp
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

    class TextChanged: TextWatcher {
        constructor(search: EditText) {
            this.search = search
            this.frgm = this.search.findFragment()
        }
        private var search: EditText
        private var frgm: TraitSelectFragment

        override fun afterTextChanged(p0: Editable?) {
            frgm.toggleTraits(search)
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
}