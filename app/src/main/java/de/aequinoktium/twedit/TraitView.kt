package de.aequinoktium.twedit

import android.content.Context
import android.database.Cursor
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
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

    lateinit var c: CharacterViewModel
    private var data = TraitData()
    private var char_trait = CharTrait()

    // the main views of the layout
    private val ll_title = LinearLayout(context)
    private val tv_name = TextView(context)
    private val tv_xp = TextView(context)
    private val tv_desc = TextView(context)
    private val ll_rank = LinearLayout(context)
    private val iv_dec_rank = ImageView(context)
    private val iv_inc_rank = ImageView(context)
    private val tv_rank = TextView(context)
    private val rg_variant1 = RadioGroup(context)
    private val rg_variant2 = RadioGroup(context)
    private val rg_variant3 = RadioGroup(context)
    private val rg_variant4 = RadioGroup(context)
    private val bt_add_trait = Button(context)


    /**
     * initializes the layout and the views
     */
    private fun init() {
        orientation = VERTICAL

        var lp = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        this.layoutParams = lp

        ll_title.orientation = HORIZONTAL
        ll_title.addView(tv_name)
        ll_title.addView(tv_xp)
        this.addView(ll_title)

        bt_add_trait.text = resources.getText(R.string.ts_add_trait)
        bt_add_trait.setOnClickListener {addTrait()}

        var views = arrayOf(
            tv_desc,
            ll_rank,
            rg_variant1,
            rg_variant2,
            rg_variant3,
            rg_variant4,
            bt_add_trait
        )

        for (v in views) {
            this.addView(v)
            v.visibility = GONE
        }

        this.background = ResourcesCompat.getDrawable(
            resources, R.drawable.simple_border,
            null
        )

        ll_title.setOnClickListener {expandView()}
        tv_name.setTypeface(null, Typeface.BOLD)

        var xp_layout = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        tv_xp.layoutParams = xp_layout
        tv_xp.gravity = Gravity.RIGHT
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
        tv_name.text = n
    }

    /**
     * is this a complex trait ...
     */
    fun setComplex(variants: Boolean, max_rank: Int) {
        if (variants or (max_rank > 1)) {
            data.complex = true
        }
    }

    /**
     * set the initial xp (only displays numbers for trivial skills)
     */
    fun setXp(xp: Int) {
        data.xp = xp
        if (!data.complex) {
            tv_xp.text = xp.toString()
        } else if (xp > 0) {
            tv_xp.text = "?"
        } else {
            tv_xp.text = "-?"
        }
    }

    /**
     * the view should be expanded (or collapsed)
     * retrieves additional trait data on first call
     */
    private fun expandView() {
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
    private suspend fun loadTrait(data: TraitData): TraitData {
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
            result.total_xp = result.rank_xp * result.min_rank
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

            var map = mutableMapOf<Int, TraitVariant>()

            if (data.variants[variant.grp] != null) {
                map = data.variants[variant.grp] as MutableMap<Int, TraitVariant>
            } else {
                data.variants[variant.grp] = mutableMapOf<Int, TraitVariant>()
                map = data.variants[variant.grp]  as MutableMap<Int, TraitVariant>
            }
            map[variant.var_id] = variant
        }

        return result
    }

    private fun updateContent() {
        tv_desc.setText(HtmlCompat.fromHtml(
            data.txt,
            HtmlCompat.FROM_HTML_MODE_LEGACY)
        )

        if (data.max_rank > 1) {
            var act = context as MainActivity

            var lp = LayoutParams(act.calc_dp(32), act.calc_dp(32))

            iv_dec_rank.setImageResource(R.drawable.arrow_left)
            iv_dec_rank.layoutParams = lp
            iv_dec_rank.setOnClickListener {
                updateRank(-1)
            }
            iv_inc_rank.setImageResource(R.drawable.arrow_right)
            iv_inc_rank.layoutParams = lp
            iv_inc_rank.setOnClickListener {
                updateRank(+1)
            }
            tv_rank.text = data.min_rank.toString()
            tv_rank.minEms = 2
            tv_rank.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

            ll_rank.gravity = Gravity.CENTER
            ll_rank.addView(iv_dec_rank)
            ll_rank.addView(tv_rank)
            ll_rank.addView(iv_inc_rank)
        }

        // create views for variants ...
        lateinit var grp: RadioGroup
        var i = 0
        for (key in data.variants.keys) {
            when (i) {
                0 -> grp = rg_variant1
                1 -> grp = rg_variant2
                2 -> grp = rg_variant3
                3 -> grp = rg_variant4
            }


            val tv_variant_title = TextView(context)
            tv_variant_title.text = key
            tv_variant_title.setTypeface(null, Typeface.BOLD)
            grp.addView(tv_variant_title)
            val trait_vars = data.variants[key] as MutableMap<Int, TraitVariant>

            for (key in trait_vars.keys) {
                val variant = trait_vars[key] as TraitVariant
                val rb = RadioButton(context)
                rb.id = variant.var_id

                var variant_name = variant.name

                if (variant.oper == 0) {
                    variant_name += " (" + variant.xp_factor.toInt().toString() + ")"
                } else {
                    when (variant.xp_factor) {
                        0.25f -> variant_name += " (x¼)"
                        0.333f -> variant_name += " (x⅓)"
                        0.5f -> variant_name += " (x½)"
                        0.666f -> variant_name += " (x⅔)"
                        0.75f -> variant_name += " (x¾)"
                        else -> variant_name += " (x" + variant.xp_factor.toInt().toString() + ")"
                    }
                }
                rb.text = variant_name

                grp.addView(rb)
                val txt = TextView(context)
                txt.text = variant.txt
                grp.addView(txt)
            }

            grp.setOnCheckedChangeListener{ grp, id ->
                onVariantSelectionChanged(grp, id)
            }

            i++
            if (i > 3) break
        }
    }

    /**
     * toggles the visibility for the detail views
     */
    private fun toggleDetails(td: TraitData) {
        this.data = td
        updateContent()
        toggleDetails()
    }
    private fun toggleDetails() {

        // set visibility for description text
        if (tv_desc.visibility == VISIBLE) {
            tv_desc.visibility = GONE
        } else {
            tv_desc.visibility = VISIBLE
        }

        // set visibility for rank selector
        if (data.max_rank > 1) {
            if (ll_rank.visibility == VISIBLE) {
                ll_rank.visibility = GONE
            } else {
                ll_rank.visibility = VISIBLE
                updateXp()
            }
        }

        // set visibility for variants ...
        var variants = arrayOf<RadioGroup>()
        var s = data.variants.size
        if (s >= 1) variants += rg_variant1
        if (s >= 2) variants += rg_variant2
        if (s >= 3) variants += rg_variant3
        if (s >= 4) variants += rg_variant4
        for (v in variants) {
            if (v.visibility == VISIBLE) {
                v.visibility = GONE
            } else {
                v.visibility = VISIBLE
            }
        }

        if (bt_add_trait.visibility == VISIBLE) {
            bt_add_trait.visibility = GONE
        } else {
            bt_add_trait.visibility = VISIBLE
        }
    }

    private fun updateRank(delta: Int) {
        var new_rank = data.cur_rank + delta
        if (new_rank <= data.min_rank) {
            new_rank = kotlin.math.max(new_rank, data.min_rank)
        } else if (new_rank >= data.max_rank) {
            new_rank = kotlin.math.min(new_rank, data.max_rank)
        }
        data.cur_rank = new_rank
        data.total_xp = new_rank * data.rank_xp
        tv_rank.text = new_rank.toString()
        tv_xp.text = data.total_xp.toString()
    }

    /**
     * Updating the xp if rank or variants are changed ...
     */
    private fun updateXp() {
        // for ranked traits
        if (data.max_rank > 1) {
            data.total_xp = data.cur_rank * data.xp
        }
        // for variant traits
        if (data.variants.size > 0) {
            var new_xp = 0f
            for (variant_group in data.variants.values) {
                for (key in variant_group.keys) {
                    var variant = variant_group[key] as TraitVariant
                    if (variant.selected) {
                        if (variant.oper == 0) {
                            new_xp += variant.xp_factor
                        } else {
                            new_xp *= variant.xp_factor
                        }
                    }
                }
            }
            if (data.total_xp > 0) {
                data.total_xp = kotlin.math.ceil(new_xp).toInt()
            } else {
                data.total_xp = kotlin.math.floor(new_xp).toInt()
            }
        }
        tv_xp.text = data.total_xp.toString()
    }

    /**
     * Listener for selection changes on radio buttons
     * updates data.variants to reflect which variants are selected
     * calls updateXp()
     */
    private fun onVariantSelectionChanged(grp: RadioGroup?, id: Int) {
        Log.d("info", "selected: $id")
        var k = -1
        when (grp) {
            rg_variant1 -> k = 0
            rg_variant2 -> k = 1
            rg_variant3 -> k = 2
            rg_variant4 -> k = 3
        }

        val keys = data.variants.keys.toTypedArray()
        val key = keys[k]
        val variants = data.variants[key] as MutableMap<Int, TraitVariant>
        for (var_key in variants.keys) {
            var variant = variants[var_key] as TraitVariant

            if (id == var_key) {
                variant.selected = true
                when (k) {
                    0 -> char_trait.var1_id = id
                    1 -> char_trait.var2_id = id
                    2 -> char_trait.var3_id = id
                    3 -> char_trait.var4_id = id
                }
            } else {
                variant.selected = false
            }
        }
        updateXp()
    }

    private fun addTrait() {
        var var_id1 = "NULL"
        var var_id2 = "NULL"
        var var_id3 = "NULL"
        var var_id4 = "NULL"
        if (char_trait.var1_id > 0) var_id1 = char_trait.var1_id.toString()
        if (char_trait.var2_id > 0) var_id2 = char_trait.var2_id.toString()
        if (char_trait.var3_id > 0) var_id3 = char_trait.var3_id.toString()
        if (char_trait.var4_id > 0) var_id4 = char_trait.var4_id.toString()

        var rank = 0
        if (data.max_rank > 1) rank = data.cur_rank

        var sql = """
            INSERT INTO char_traits (
                char_id, 
                trait_id,
                rank,
                var1_id,
                var2_id,
                var3_id,
                var4_id,
                xp_cost,
                name, 
                txt
            ) VALUES (
                ${c.char_id},
                ${data.id},
                $rank,
                $var_id1,
                $var_id2,
                $var_id3,
                $var_id4,
                ${data.total_xp},
                '${data.name}',
                ''
            )
        """.trimIndent()
        c.db.execSQL(sql)

        sql = """
            UPDATE
                char_core
            SET
                xp_used = xp_used + ${data.total_xp}
            WHERE
                id = ${c.char_id}
        """.trimIndent()
        c.db.execSQL(sql)
    }
}