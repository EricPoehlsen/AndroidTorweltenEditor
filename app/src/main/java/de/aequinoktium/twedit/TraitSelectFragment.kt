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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var rv_container: RecyclerView
    private lateinit var rv_manager: LinearLayoutManager
    private lateinit var rv_adapter: TraitSelectAdapter
    private lateinit var te_search: EditText
    private lateinit var b_toggle: Button
    private var traits = arrayOf<TraitData>()

    /**
     * Creates the view for this Fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trait_select, container, false)
    }

    /**
     * Initiates the View once it has been created
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity
        rv_container = act.findViewById(R.id.traitselect_recycler)
        rv_manager = LinearLayoutManager(act)
        rv_adapter = TraitSelectAdapter(traits, c)
        rv_container.apply {
            setHasFixedSize(true)
            layoutManager = rv_manager
            adapter = rv_adapter
        }

        c.viewModelScope.launch(Dispatchers.IO) {
            traits = loadTraits()
            withContext(Dispatchers.Main) {
                rv_adapter.updateData(traits)
            }
        }


        b_toggle = act.findViewById<Button>(R.id.traitselect_toggle)
        b_toggle.setOnClickListener{v -> toggleTraits(v)}

        te_search = act.findViewById<EditText>(R.id.traitselect_search)

        val watcher = TextChanged(te_search)
        te_search.addTextChangedListener(watcher)
    }

    /**
     * filters the trait list based on search and (dis)advantages toggle button
     */
    fun toggleTraits(view: View){
        var filtered_traits = arrayOf<TraitData>()

        if (view == b_toggle) {
            toggle += 1
            if (toggle > 2) toggle = 0
        }
        var search = te_search.text.toString().toLowerCase(c.LOCALE)

        for (trait in traits) {
            val name = trait.name.toLowerCase(c.LOCALE)
            if (search.length > 0 && search !in name) continue

            if (toggle == 0) {
                filtered_traits += trait
                b_toggle.text = getText(R.string.ts_all)
            } else if (toggle == 1 && trait.xp > 0) {
                filtered_traits += trait
                b_toggle.text = getText(R.string.ts_advantages)
            } else if (toggle == 2 && trait.xp < 0) {
                filtered_traits += trait
                b_toggle.text = getText(R.string.ts_disadvantages)
            }
        }

        rv_adapter.updateData(filtered_traits)
    }

    /**
     * Load available traits from the SQLite database
     * @return an Array of [TraitData]
     */
    fun loadTraits(): Array<TraitData> {
        var traits = arrayOf<TraitData>()
        var sql = """
            SELECT
                id,
                name, 
                cls, 
                grp,
                txt,
                min_rank, 
                max_rank,
                xp_cost
            FROM 
                traits
            ORDER BY
            cls, grp
        """.trimIndent()
        var data = c.db.rawQuery(sql, null)
        while (data.moveToNext()) {
            var trait = TraitData()
            trait.id = data.getInt(0)
            trait.name = data.getString(1)
            trait.cls = data.getInt(2)
            trait.grp = data.getInt(3)
            trait.txt = data.getString(4)
            trait.min_rank = data.getInt(5)
            trait.max_rank = data.getInt(6)
            trait.xp = data.getInt(7)

            sql = """
                SELECT 
                    id,
                    name,
                    xp_factor,
                    oper, 
                    grp,
                    txt
                FROM 
                    trait_vars
                WHERE trait_id = ${trait.id}
            """.trimIndent()
            val variants: MutableMap<String, MutableMap<Int, TraitVariant>> = mutableMapOf()
            val trait_vars = c.db.rawQuery(sql, null)
            while (trait_vars.moveToNext()) {
                val variant = TraitVariant()
                variant.var_id = trait_vars.getInt(0)
                variant.name = trait_vars.getString(1)
                variant.xp_factor = trait_vars.getFloat(2)
                variant.oper = trait_vars.getInt(3)
                variant.grp = trait_vars.getString(4)
                variant.txt = trait_vars.getString(5)
                if (variants[variant.grp] == null) {
                    variants[variant.grp] = mutableMapOf(Pair(variant.var_id, variant))
                } else {
                    variants[variant.grp]?.set(variant.var_id, variant)
                }
            }
            trait_vars.close()
            trait.variants = variants
            traits += trait
        }
        data.close()
        return traits
    }

    /**
     * [TextWatcher] implementation to read changes in the search field
     * calls the [toggleTraits] method.
     */
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