package de.aequinoktium.twedit

import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class TraitView: LinearLayout {
    constructor(context: Context?) : super(context) {
        init()
    }
    constructor(context: Context?, attrs: AttributeSet): super(context, attrs){
        init()
    }
    constructor(context: Context?, attrs: AttributeSet, defStyleAttr: Int):
            super(context, attrs, defStyleAttr){
        init()
    }

    /**
     * Object to hold the data of a trait
     */
    class TraitData {
        var id = 0
        var name = ""
        var xp = 0
        var txt = ""
        var min_rank = 0
        var max_rank = 0
        var cur_rank = 0
        var new_rank = 0
        var rank_xp = 0
        var total_xp = 0
        var cls = 0
        var grp = 0
        var variants: MutableMap<String, Array<TraitVariant>> = mutableMapOf()
        var complex = false
    }

    class TraitVariant {
        var var_id = 0
        var grp = ""
        var xp_factor = 0f
        var oper = 0
        var name = ""
        var txt = ""
        var selected = false
    }

    lateinit var c: CharacterViewModel
    private var data = TraitData()

    private var vName = TextView(context)
    private var vXp = TextView(context)
    private var vTitle = LinearLayout(context)
    lateinit private var vText: TextView
    lateinit private var vRanks: LinearLayout
    lateinit private var vDecRank: ImageView
    lateinit private var vIncRank: ImageView
    lateinit private var vRank: TextView
    lateinit private var vVariant1: RadioGroup
    lateinit private var vVariant2: RadioGroup
    lateinit private var vVariant3: RadioGroup
    lateinit private var vVariant4: RadioGroup


    /**
     * initialize the layout with necessary views
     */
    fun init() {
        this.orientation = VERTICAL
        this.background = ResourcesCompat.getDrawable(
            resources, R.drawable.simple_border,
            null
        )

        this.addView(vTitle)
        var title_layout = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        vTitle.layoutParams = title_layout
        vTitle.orientation = HORIZONTAL
        vTitle.addView(vName)
        vTitle.addView(vXp)
        vTitle.setOnClickListener {expandView()}
        vName.setTypeface(null, Typeface.BOLD)

        var xp_layout = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        vXp.layoutParams = xp_layout
        vXp.gravity = Gravity.RIGHT
    }

    /**
     * set the trait id which acts as primary selector
     * for all database requests concerning the trait
     * @param id
     */
    fun setTraitId(id: Int) {
        data.id = id
    }

    /**
     * set the trait name
     * @param n name of the trait
     */
    fun setName(n: String) {
        data.name = n
        vName.text = n
    }

    fun setComplex(variants: Boolean, max_rank: Int) {
        if (variants or (max_rank > 1)) {
            data.complex = true
        }
    }

    fun setXp(xp: Int) {
        data.xp = xp
        if (!data.complex) {
            vXp.text = xp.toString()
        } else if (xp > 0) {
            vXp.text = "?"
        } else {
            vXp.text = "-?"
        }
    }

    fun expandView() {
        if (data.max_rank == 0) {
            c.viewModelScope.launch {
                var traitData = loadTrait(data)
                withContext(Dispatchers.Main) {
                    toggleDetails(traitData)
                }
            }
        } else {
            toggleDetails()
        }
    }

    /**
     * Retrieve all information for this trait from the database
     * @param data a TraitData object which needs to contain an id
     * @return a TraitData object with all available trait data from the database
     */
    suspend fun loadTrait(data: TraitData): TraitData {
        val result = data
        var sql = """
            SELECT 
                name,
                min_rank, 
                max_rank, 
                txt,
                cls,
                grp,
                xp_cost
            FROM 
                traits 
            WHERE 
                id = ${data.id}
        """.trimMargin()
        var trait: Cursor = c.db.rawQuery(sql, null)
        if (trait.count > 0) {
            trait.moveToFirst()
            result.name = trait.getString(0)
            result.min_rank = trait.getInt(1)
            result.cur_rank = trait.getInt(1)
            result.max_rank = trait.getInt(2)
            result.txt = trait.getString(3)
            result.cls = trait.getInt(4)
            result.grp = trait.getInt(5)
            result.rank_xp = trait.getInt(6)
        }
        trait.close()

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
            WHERE
                trait_id = ${data.id}
        """.trimIndent()
        var trait_var = c.db.rawQuery(sql, null)
        while (trait_var.moveToNext()) {
            var variant = TraitVariant()
            variant.var_id = trait_var.getInt(0)
            variant.name = trait_var.getString(1)
            variant.xp_factor = trait_var.getFloat(2)
            variant.oper = trait_var.getInt(3)
            variant.grp = trait_var.getString(4)
            variant.txt = trait_var.getString(5)

            var entry = arrayOf(variant)

            if (data.variants[variant.grp] != null) {
                var new_array = data.variants[variant.grp] as Array<TraitVariant>
                new_array += entry
                data.variants[variant.grp] = new_array
            } else {
                data.variants[variant.grp] = entry
            }
        }

        return result
    }

    fun toggleDetails(td: TraitData) {
        this.data = td
        toggleDetails()
    }
    fun toggleDetails() {
        updateXp()

        if (!this::vText.isInitialized) {
            vText = TextView(context)
            this.addView(vText)
            vText.setText(HtmlCompat.fromHtml(
                data.txt,
                HtmlCompat.FROM_HTML_MODE_LEGACY)
            )
            vText.visibility = GONE

            if (data.max_rank > 1) {
                vRanks = LinearLayout(context)
                vRanks.orientation = HORIZONTAL
                vDecRank = ImageView(context)
                vRank = TextView(context)
                vIncRank = ImageView(context)

                var act = context as MainActivity

                var lp = LayoutParams(act.calc_dp(32), act.calc_dp(32))


                vDecRank.setImageResource(R.drawable.arrow_left)
                vDecRank.layoutParams = lp
                vDecRank.setOnClickListener {
                    updateRank(-1)
                }
                vIncRank.setImageResource(R.drawable.arrow_right)
                vIncRank.layoutParams = lp
                vIncRank.setOnClickListener {
                    updateRank(+1)
                }
                vRank.text = data.min_rank.toString()
                vRank.minEms = 2
                vRank.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                vRanks.gravity = Gravity.CENTER
                vRanks.addView(vDecRank)
                vRanks.addView(vRank)
                vRanks.addView(vIncRank)

                this.addView(vRanks)

                vRanks.visibility = GONE
            }

            // create views for variants ...
            lateinit var grp: RadioGroup
            var i = 0
            for (key in data.variants.keys) {
                when (i) {
                    0 -> {
                        vVariant1 = RadioGroup(context)
                        grp = vVariant1
                    }
                    1 -> {
                        vVariant2 = RadioGroup(context)
                        grp = vVariant2
                    }
                    2 -> {
                        vVariant3 = RadioGroup(context)
                        grp = vVariant3
                    }
                    3 -> {
                        vVariant4 = RadioGroup(context)
                        grp = vVariant4
                    }
                }

                var vVarTitle = TextView(context)
                vVarTitle.text = key
                grp.addView(vVarTitle)
                var trait_vars = data.variants[key] as Array<TraitVariant>
                for (variant in trait_vars) {
                    var rb = RadioButton(context)
                    rb.text = variant.name
                    grp.addView(rb)
                    var txt = TextView(context)
                    txt.text = variant.txt
                    grp.addView(txt)
                }

                grp.setOnCheckedChangeListener{ grp, id ->
                    onVariantSelectionChanged(grp, id)
                }


                this.addView(grp)
                grp.visibility = GONE


                i++
                if (i > 3) break
            }





        }

        // set visibility for description text
        if (vText.visibility == VISIBLE) {
            vText.visibility = GONE
        } else {
            vText.visibility = VISIBLE
        }

        // set visibility for rank selector
        if (data.max_rank > 1) {
            if (vRanks.visibility == VISIBLE) {
                vRanks.visibility = GONE
            } else {
                vRanks.visibility = VISIBLE
            }
        }

        // set visibility for variants ...
        var variants = arrayOf<RadioGroup>()
        var s = data.variants.size
        if (s >= 1) variants += vVariant1
        if (s >= 2) variants += vVariant2
        if (s >= 3) variants += vVariant3
        if (s >= 4) variants += vVariant4
        for (v in variants) {
            if (v.visibility == VISIBLE) {
                v.visibility = GONE
            } else {
                v.visibility = VISIBLE
            }
        }
   }

    fun updateRank(delta: Int) {
        var new_rank = data.cur_rank + delta
        if (new_rank <= data.min_rank) {
            new_rank = kotlin.math.max(new_rank, data.min_rank)
        } else if (new_rank >= data.max_rank) {
            new_rank = kotlin.math.min(new_rank, data.max_rank)
        }
        data.cur_rank = new_rank
        data.total_xp = new_rank * data.rank_xp
        vRank.text = new_rank.toString()
        vXp.text = data.total_xp.toString()
    }

    fun updateXp() {
        if (data.max_rank > 1) {
            data.total_xp = data.cur_rank * data.xp
            vXp.text = data.total_xp.toString()
        }
    }

    fun onVariantSelectionChanged(grp: RadioGroup?, id: Int) {
        Log.d("info", id.toString())
    }
}