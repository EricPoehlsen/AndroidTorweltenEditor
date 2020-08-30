package de.aequinoktium.twedit

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.children
import androidx.core.view.size
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Text

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
    private var show_details = false
    private var selected_variants = mutableMapOf<String, Int>()

    // the main views of the layout
    private val ll_title = LinearLayout(context)
    private val tv_name = TextView(context)
    private val tv_xp = TextView(context)
    private val tv_desc = TextView(context)
    private val ll_rank = LinearLayout(context)
    private val iv_dec_rank = ImageView(context)
    private val iv_inc_rank = ImageView(context)
    private val tv_rank = TextView(context)
    private var var_grps = arrayOf<RadioGroup>()
    private val bt_add_trait = Button(context)


    /**
     * initializes the layout and the views
     */
    private fun init() {
        orientation = VERTICAL

        val lp = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        val act = context as MainActivity
        lp.topMargin = act.calc_dp(4)


        this.layoutParams = lp

        ll_title.orientation = HORIZONTAL
        ll_title.addView(tv_name)
        ll_title.addView(tv_xp)
        this.addView(ll_title)

        bt_add_trait.text = resources.getText(R.string.ts_add_trait)
        bt_add_trait.setOnClickListener {
            c.viewModelScope.launch(Dispatchers.IO) {
                addTrait()
                withContext(Dispatchers.Main) {
                    updateName()
                }
            }
        }

        val views = arrayOf(
            tv_desc,
            ll_rank,
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

        ll_title.setOnClickListener {toggleDetails()}
        tv_name.setTypeface(null, Typeface.BOLD)

        val xp_layout = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        tv_xp.layoutParams = xp_layout
        tv_xp.gravity = Gravity.END
    }

    fun setTrait(trait: TraitData) {
        this.data = trait

        updateName()

        if (!data.complex) {
            tv_xp.text = data.xp.toString()
        } else if (data.xp > 0) {
            tv_xp.text = "?"
        } else {
            tv_xp.text = "-?"
        }

        updateContent()

    }

    private fun updateName() {
        var display_name = data.name
        if (data.id in c.char_traits) {
            display_name += " ✔"
            tv_name.setTextColor(resources.getColor(R.color.Blue))
        }
        tv_name.text = display_name
    }

    /**
     * Get the name of this trait
     */
    fun getName(): String {
        return data.name
    }

    /**
     * allows access to the xp value of a trait
     */
    fun getXp(): Int {
        return data.xp
    }

    private fun updateContent() {
        tv_desc.setText(HtmlCompat.fromHtml(
            data.txt,
            HtmlCompat.FROM_HTML_MODE_LEGACY)
        )

        updateRank(0)
        if (data.max_rank > 1) {
            val act = context as MainActivity

            val lp = LayoutParams(act.calc_dp(32), act.calc_dp(32))

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
            ll_rank.removeAllViews()
            ll_rank.addView(iv_dec_rank)
            ll_rank.addView(tv_rank)
            ll_rank.addView(iv_inc_rank)
        }

        // create views for variants ...
        var i = 0
        for (key in data.variants.keys) {
            val grp = RadioGroup(context)
            val tv_variant_title = TextView(context)
            tv_variant_title.text = key
            tv_variant_title.setTypeface(null, Typeface.BOLD)
            grp.addView(tv_variant_title)
            val trait_vars = data.variants[key] as MutableMap<Int, TraitVariant>

            for (var_key in trait_vars.keys) {
                val variant = trait_vars[var_key] as TraitVariant
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
                txt.setText(HtmlCompat.fromHtml(
                    variant.txt,
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
                )
                grp.addView(txt)
            }

            grp.setOnCheckedChangeListener{ it, id ->
                onVariantSelectionChanged(it, id)
            }
            var_grps += grp
        }

        var cur_index = indexOfChild(bt_add_trait)
        for (grp in var_grps) {
            grp.visibility = GONE
            if (grp !in this.children) {
                this.addView(grp, cur_index)
            }
            cur_index++
        }
    }

    fun resetView() {
        ll_rank.removeAllViews()
        for (grp in var_grps) {
            this.removeView(grp)
        }
        var_grps = arrayOf<RadioGroup>()
        tv_desc.text = ""
        tv_name.text = ""
        tv_name.setTextColor(resources.getColor(R.color.Grey))
        hideDetails()
    }

    private fun toggleDetails() {
        if (show_details) {
            hideDetails()
            show_details = false
        } else {
            showDetails()
            show_details = true
        }
    }

    private fun hideDetails() {
        tv_desc.visibility = GONE
        ll_rank.visibility = GONE

        // set visibility for variants ...
        for (v in var_grps) {
            v.visibility = GONE
        }

        bt_add_trait.visibility = GONE
    }

    private fun showDetails() {
        tv_desc.visibility = VISIBLE

        if (data.max_rank > 1) {
            ll_rank.visibility = VISIBLE
            updateXp()
        }

        // set visibility for variants ...
        for (v in var_grps) {
            v.visibility = VISIBLE
        }

        bt_add_trait.visibility = VISIBLE
    }

    private fun updateRank(delta: Int) {
        var new_rank = data.cur_rank + delta
        if (new_rank <= data.min_rank) {
            new_rank = kotlin.math.max(new_rank, data.min_rank)
        } else if (new_rank >= data.max_rank) {
            new_rank = kotlin.math.min(new_rank, data.max_rank)
        }
        data.cur_rank = new_rank
        data.total_xp = new_rank * data.xp
        tv_rank.text = new_rank.toString()
        tv_xp.text = data.total_xp.toString()
    }

    /**
     * Updating the xp if rank or variants are changed ...
     */
    private fun updateXp() {
        data.total_xp = data.cur_rank * data.xp


        // for variant traits
        if (data.variants.isNotEmpty()) {
            var new_xp = 0f
            for (k in selected_variants.keys) {
                val variant = data.variants[k]?.get(selected_variants[k]) as TraitVariant
                if (variant.oper == 0) {
                    new_xp += variant.xp_factor
                } else {
                    new_xp *= variant.xp_factor
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
        val g = grp as RadioGroup
        val t = g.getChildAt(0) as TextView
        selected_variants[t.text.toString()] = id

        updateXp()
    }

    private suspend fun addTrait() {
        var variants = ""
        for (k in selected_variants.keys) {
            variants += selected_variants.get(k).toString() + " "
        }
        variants = variants.trim()

        var rank = 1
        if (data.max_rank > 1) rank = data.cur_rank
        handleEffects()
        var sql = """
            INSERT INTO char_traits (
                char_id, 
                trait_id,
                rank,
                variants,
                xp_cost,
                name, 
                txt, 
                effects
            ) VALUES (
                ${c.char_id},
                ${data.id},
                $rank,
                '$variants',
                ${data.total_xp},
                '${data.name}',
                '',
                '${data.effects}'
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
        c.char_traits += data.id
    }

    /**
     * handler for effects which are defined in the traits
     */
    suspend fun handleEffects() {
        if (data.effects.length > 0) {
            var applied_effects = ""
            val effects = data.effects.split(",")
            for (effect in effects) {
                if (effect.startsWith("money")) {
                    applied_effects += moneyEffect(effect)
                }
            }

        data.effects = applied_effects.dropLast(1)
        }

    }

    /**
     * the money effect denoted by the 'money:0000' effect
     */
    suspend fun moneyEffect(effect: String): String {
        val amount = effect.replace("money:", "").toFloat() * data.cur_rank
        c.moneyTransaction(0, c.primaryAccount().nr, amount, data.name)
        return "money:$amount,"
    }
}