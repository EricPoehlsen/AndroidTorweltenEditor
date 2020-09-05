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
    private lateinit var et_quantity: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog_item = d.current_catalog_item

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
        var buyable_qualities = ArrayList<String>()
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
        sp.setSelection(3)
    }

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

    /**
     * calculate the item weight based on base weight and chosen variants
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
     */
    fun weightText(weight: Int): String {
        var result = 0f
        val label = getString(R.string.cinv_weight)
        var unit = "g"
        if (weight > 1000) {
            result = (weight/1000).toFloat()
            unit = "kg"
        } else {
            result = weight.toFloat()
        }
        return "${label} ${result}${unit}"
    }

    /**
     * calculate price from base price, selected variants and quality
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
     * set the quality of an item
     */
    fun setQuality(qual: Int) {
        item.orig_qual = qual
        item.cur_qual = qual
        update()
    }

    fun setQuantity(qty: Int) {
        item.qty = qty
        update()
    }

    /**
     * construct the item name based on the selected variants
     */
    fun buildName(): String {
        var name = catalog_item.name
        for (all in catalog_item.variants.values) {
            for (variant in all) {
                if (variant.selected) {
                    if (variant.edit_name) {
                        if (variant.name.endsWith(" ")){
                            name = variant.name + name
                        } else {
                            name = variant.name + name.toLowerCase(Locale.getDefault())
                        }
                    }
                    if (variant.override_name) {
                        name = variant.name
                    }
                }
            }
        }
        return name
    }

    fun update() {
        item.name = buildName()
        tv_name.text = item.name
        item.weight = calcWeight()
        tv_weight.text = weightText(item.weight)
        item.price = calcPrice()
        tv_price.text = priceText(item.price)
    }

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

    class SelectListener(val name:String, val frgm: CatalogItemFragment): AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
            Log.d("info", name)
            val variants = frgm.catalog_item.variants[name]
            for (variant in variants!!) {
                variant.selected = false
            }
            variants[selected].selected = true
            frgm.update()
        }

        // unused - necessary for implementation
        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

    class QualityListener(val frgm: CatalogItemFragment): AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
            frgm.setQuality(pos + 3)
        }

        // unused - necessary for implementation
        override fun onNothingSelected(p0: AdapterView<*>?) {}
    }

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

        }

        // unused - necessary for class implementation
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }
}