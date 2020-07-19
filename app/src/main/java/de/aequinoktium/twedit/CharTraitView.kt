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

class CharTraitView: LinearLayout {
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
    var trait_vars = mutableMapOf<Int, TraitVariant>()
    var char_trait = CharTrait()


    // the main views of the layout
    private val ll_title = LinearLayout(context)
    private val tv_name = TextView(context)
    private val tv_xp = TextView(context)
    private val tv_desc = TextView(context)
    private val tv_rank = TextView(context)
    private val ll_more_info = LinearLayout(context)


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
        ll_title.addView(tv_rank)
        tv_rank.visibility = GONE
        ll_title.addView(tv_xp)
        this.addView(ll_title)

        var views = arrayOf(
            tv_desc,
            ll_more_info
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
        tv_xp.gravity = Gravity.END

        ll_more_info.orientation = VERTICAL
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

    fun setRank(rank: Int) {
        if (rank > 0) {
            tv_rank.text = " (" + rank.toString() + ") "
            tv_rank.visibility = VISIBLE
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
                var trait_data = loadTrait(data)
                withContext(Dispatchers.Main) {
                    toggleDetails(trait_data)
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
        var result = TraitData()
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
        if (trait.moveToFirst()) {
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
            trait_vars[variant.var_id] = variant
        }
        return result
    }

    private fun updateContent() {
        var html_text = ""
        if (char_trait.txt.length > 1) {
            html_text = char_trait.txt
        } else {
            html_text = data.txt
        }
        tv_desc.setText(HtmlCompat.fromHtml(
            html_text,
            HtmlCompat.FROM_HTML_MODE_LEGACY)
        )

        // create views for variants ...
        var variants = arrayOf(
            char_trait.var1_id,
            char_trait.var2_id,
            char_trait.var3_id,
            char_trait.var4_id
        )

        for (var_id in variants) {
            if (var_id > 0) {
                val tv_variant_title = TextView(context)
                tv_variant_title.text = trait_vars[var_id]?.grp
                ll_more_info.addView(tv_variant_title)

                val tv_variant_name = TextView(context)
                tv_variant_name.text = trait_vars[var_id]?.name
                ll_more_info.addView(tv_variant_name)

                val tv_variant_text = TextView(context)
                tv_variant_text.text = trait_vars[var_id]?.txt
                ll_more_info.addView(tv_variant_text)
            }
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

        if (ll_more_info.visibility == VISIBLE) {
            ll_more_info.visibility = GONE
        } else {
            ll_more_info.visibility = VISIBLE
        }
    }

    fun getCharTraitId(): Int {
        return char_trait.id
    }

}