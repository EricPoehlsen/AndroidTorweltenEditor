package de.aequinoktium.twedit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class CatalogItemFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private val d: DataViewModel by activityViewModels()
    private lateinit var catalog_item: CatalogItem
    private var item = Item()

    private lateinit var ll_variants: LinearLayout
    private lateinit var tv_name: TextView
    private lateinit var tv_price: TextView
    private lateinit var tv_weight: TextView
    private lateinit var tv_damage: TextView
    private lateinit var tv_weight_limit: TextView
    private lateinit var et_quantity: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog_item = d.current_catalog_item
        setInitialData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(
            R.layout.fragment_catalog_item,
            container,
            false
        )
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ll_variants = view.findViewById(R.id.catalog_item_variants)

        // add variants
        for (name in catalog_item.variants.keys) {
            addVariant(name)
        }

        setupQualitySpinner(view)

        tv_name = view.findViewById<TextView>(R.id.catalog_item_name)
        tv_name.text = catalog_item.name
        tv_weight = view.findViewById(R.id.catalog_item_weight)
        tv_weight.text = weightText(calcWeight())
        tv_price = view.findViewById(R.id.catalog_item_price)
        tv_price.text = priceText(calcPrice())
        tv_damage = view.findViewById(R.id.catalog_item_dmg)
        displayDamage()
        tv_weight_limit = view.findViewById(R.id.catalog_item_weight_limit)
        displayWeightLimit()

        et_quantity = view.findViewById(R.id.catalog_item_quantity)
        et_quantity.addTextChangedListener(QuantityListener(et_quantity, this))

        val bt_take = view.findViewById<Button>(R.id.catalog_item_take)
        bt_take.setOnClickListener{ addItem(false) }
        val bt_buy = view.findViewById<Button>(R.id.catalog_item_buy)
        bt_buy.setOnClickListener{ addItem(true) }
    }

    fun setupQualitySpinner(parent: View) {
        val sp = parent.findViewById<Spinner>(R.id.catalog_item_quality)

        val quality_names = resources.getStringArray(R.array.cinv_qualities)
        val buyable_qualities = ArrayList<String>()
        for (i in 3..9) {
            buyable_qualities.add(quality_names[i])
        }
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            buyable_qualities
        )
        sp.adapter = adapter
        sp.onItemSelectedListener = QualityListener(this)
        sp.setSelection(3) // normal quality
    }

    /**
     * adds label and spinner for item variants.
     */
    fun addVariant(name: String) {
        val tv = TextView(context)
        tv.text = name
        tv.id = View.generateViewId()
        ll_variants.addView(tv)

        val sp = Spinner(context)

        val names = ArrayList<String>()
        for (variant in catalog_item.variants[name]!!) {
            names.add(variant.name)
        }
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            names
        )
        sp.adapter = adapter

        sp.onItemSelectedListener = SelectListener(name,this)

        sp.id = View.generateViewId()
        ll_variants.addView(sp)
    }

    // transfer some initial data
    fun setInitialData() {
        item.container_name = catalog_item.container_name
        item.name = catalog_item.name
        item.desc = catalog_item.desc
        item.price = catalog_item.price
        item.weight = catalog_item.weight
        item.weight_limit = catalog_item.weight_limit
    }


    /**
     * calculate the item weight based on base weight and chosen variants
     * @return item weight in grams
     */
    fun calcWeight(): Int {
        var weight = catalog_item.weight
        for (all in catalog_item.variants.values) {
            for (variant in all) {
                if (variant.selected) weight = (weight * variant.weight_factor).toInt()
            }
        }
        return weight
    }

    /**
     * construct the display text for the item weight
     * @return the text to be displayed on a TextView
     */
    fun weightText(weight: Int): String {
        var value = weight.toString()
        val label = getString(R.string.cinv_weight)
        var unit = "g"
        if (weight >= 1000) {
            if (weight % 1000 == 0) {
                value = (weight / 1000).toString()
            } else {
                value = (weight / 1000).toFloat().toString()
            }
            unit = "kg"
        }
        return "${label} ${value} ${unit}"
    }

    /**
     * calculate price from base price, selected variants and quality
     * @return the item price (per piece)
     */
    fun calcPrice(): Float {
        var price = catalog_item.price
        // account for variants
        for (all in catalog_item.variants.values) {
            for (variant in all) {
                if (variant.selected){
                    price *= variant.price_factor
                }
            }
        }
        // account for quality
        val quality_price_factor = arrayOf(.1f,.25f,.5f,1f,1.5f,2.5f,5f)
        if (item.cur_qual in 3..9) {
            price *= quality_price_factor[item.cur_qual-3]
        }
        return price
    }

    /**
     * construct the string that is displayed for the price
     * also adds total price if quantity > 1
     * @return the text to be displayed on a TextView
     */
    fun priceText(price: Float):String {
        val label = getString(R.string.cinv_price)
        val amount = getString(R.string.cinv_money, price)
        var qty_amount = ""
        var qty = ""
        if (item.qty > 1) {
            qty = "${item.qty} x "
            val total = getString(R.string.cinv_money, price * item.qty)
            qty_amount = "\n= $total"
        }
        return "$label $qty$amount$qty_amount"
    }

    /**
     * calculate the weight limit based on the selected variants
     * @return weight limit in grams
     */
    fun calcWeightLimit(): Int {
        var weight_limit = catalog_item.weight_limit
        for (all in catalog_item.variants.values) {
            for (variant in all) {
                if (variant.selected){
                    weight_limit += variant.weight_limit
                }
            }
        }
        return weight_limit
    }

    /**
     * construct the text to be displayed on the weight limit TextView
     * @param weight_limit in grams
     * @return the text
     */
    fun weightLimitText(weight_limit: Int): String {
        val label = getString(R.string.cinv_weight_limit)
        var capacity = weight_limit.toString()
        var unit = "g"
        if (weight_limit >= 1000) {
            capacity = (weight_limit / 1000).toString()
            unit = "kg"
        }
        return "$label $capacity $unit"
    }

    /**
     * displays the weight limit or hides the view
     * calls setWeightLimit in the process
     */
    fun displayWeightLimit() {
        setWeightLimit()
        if (item.weight_limit > 0) {
            tv_weight_limit.visibility = View.VISIBLE
            tv_weight_limit.text = weightLimitText(item.weight_limit)
        } else {
            tv_weight_limit.visibility = View.GONE
        }
    }

    fun calcDamage(): String {
        var is_dmg_mod = false

        var dmg = catalog_item.dmg

        val dmg_elements = dmg.split("/")
        var d = 0
        var s = 0
        var t = ""
        return ""
    }

    /**
     * set the quality of an item
     * @param qual is item quality level (valid is 0..12 but we only use 3..9 in the catalog)
     */
    fun setQuality(qual: Int) {
        item.orig_qual = qual
        item.cur_qual = qual
    }

    /**
     * set the item quantity
     * @param qty the quantity ... this should be a positiv integer value
     */
    fun setQuantity(qty: Int) {
        item.qty = qty
    }

    fun setMaterial(mat: String) {
        item.material = mat
    }

    fun setDamage() {

    }

    fun setWeightLimit() {
        item.weight_limit = calcWeightLimit()
    }

    fun displayDamage() {
        if (item.dmg.isBlank()) {
            tv_damage.visibility = View.GONE
        } else {
            val text = "${getString(R.string.cinv_damage)}: ${item.dmg}"
            tv_damage.visibility = View.VISIBLE
            tv_damage.text = text
        }
    }

    /**
     * construct the item name based on the selected variants
     */
    fun buildName(): String {
        var name = catalog_item.name
        for (all in catalog_item.variants.values) {
            for (variant in all) {
                if (variant.selected) {
                    if (variant.prefix) {
                        if (variant.name.endsWith(" ")){
                            name = variant.name + name
                        } else {
                            name = variant.name + name.toLowerCase(Locale.getDefault())
                        }
                    }
                    if (variant.suffix) {
                        if (variant.name.startsWith(" ")){
                            name = name + variant.name
                        } else {
                            name = name + variant.name.toLowerCase(Locale.getDefault())
                        }
                    }
                    if (variant.rename) {
                        name = variant.name
                    }
                }
            }
        }
        return name
    }



    /**
     * recalculate price an weight and display results
     */
    fun update() {
        item.name = buildName()
        tv_name.text = item.name
        item.weight = calcWeight()
        tv_weight.text = weightText(item.weight)
        item.price = calcPrice()
        tv_price.text = priceText(item.price)
        displayDamage()
        displayWeightLimit()
    }

    /**
     * add the item to the characters inventory
     * if the character has to pay for the item, the money transaction is also initiated
     * @param pay true if character has to pay.
     */
    fun addItem(pay: Boolean) {
        c.viewModelScope.launch(Dispatchers.IO) {
            c.addToInventory(item)
            if (pay) {
                c.moneyTransaction(
                    c.primaryAccount().nr,
                    0,
                    item.price * item.qty,
                    getString(R.string.cinv_bought, item.qty, item.name)
                )
            }
        }
    }

    /**
     * Implements an [AdapterView.OnItemSelectedListener] to track the
     * users selection of a item variant. Updates the selection in the
     * [CatalogItem.variants] and calls .update() on the fragment
     *
     */
    class SelectListener(val name:String, val frgm: CatalogItemFragment): AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
            val variants = frgm.catalog_item.variants[name]
            for (variant in variants!!) {
                variant.selected = false
            }
            variants[selected].selected = true

            // handle specific data
            if (name == frgm.resources.getString(R.string.cinv_material)) {
                Log.d("Info", "Material: ${variants[selected].name}")
                frgm.setMaterial(variants[selected].name)
            }

            frgm.setDamage()
            frgm.setWeightLimit()



            frgm.update()
        }

        // unused - necessary for implementation
        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    /**
     * Implements an [AdapterView.OnItemSelectedListener] to track the
     * users quality selection and calls .setQuality on the fragment
     */
    class QualityListener(val frgm: CatalogItemFragment): AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
            frgm.setQuality(pos + 3)
            frgm.update()
        }

        // unused - necessary for implementation
        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    /**
     * Implements an [TextWatcher] to read the quantity entry and
     * update the fragment accordingly
     */
    class QuantityListener(val et: EditText, val frgm: CatalogItemFragment): TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            val input = p0.toString()
            if (!input.isBlank() && input.matches("\\d+".toRegex())) {
                var quantity = input.toInt()
                if (quantity == 0) {
                    et.setText("1")
                    quantity = 1
                }
                frgm.setQuantity(quantity)
            } else {
                frgm.setQuantity(1)
            }
            frgm.update()

        }

        // unused - necessary for class implementation
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
}