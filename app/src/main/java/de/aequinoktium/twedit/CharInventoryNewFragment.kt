package de.aequinoktium.twedit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class CharInventoryNewFragment : Fragment(){
    private val c: CharacterViewModel by activityViewModels()

    private lateinit var et_name: EditText
    private lateinit var et_desc: EditText
    private lateinit var et_weight: EditText
    private lateinit var et_price: EditText
    private lateinit var et_quality: EditText
    private lateinit var tv_weight_unit: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    /**
     * inflate the view
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View

        root = inflater.inflate(
            R.layout.fragment_char_inventory_new,
            container,
            false
        )

        return root
    }


    /**
     * initialize the views and listeners
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_name = view.findViewById(R.id.newitem_name)
        et_desc = view.findViewById(R.id.newitem_desc)
        et_quality = view.findViewById(R.id.newitem_quality)
        et_quality.addTextChangedListener(TextChanged(et_quality))
        et_price = view.findViewById(R.id.newitem_price)
        et_weight = view.findViewById(R.id.newitem_weight)
        tv_weight_unit = view.findViewById(R.id.newitem_weight_unit)
        tv_weight_unit.setOnClickListener{switchWeightUnit()}


        val bt_take = view.findViewById<Button>(R.id.newitem_take)
        bt_take.setOnClickListener {
            addItem()
        }
        val bt_buy = view.findViewById<Button>(R.id.newitem_buy)
        bt_buy.setOnClickListener {
            addItem(pay=true)
        }


    }

    /**
     * prepares the item to be added to the character and
     * hands over item storage to [CharacterViewModel]
     */
    fun addItem(pay: Boolean = false) {
        var item = Item(c)

        // name
        var name = et_name.text.toString()
        name = name.replace("'", "\u2019")
        if (name.length > 0) item.name = name

        // description
        var desc = et_desc.text.toString()
        desc = desc.replace("'", "\u2019")
        if (desc.length > 0) item.desc = desc

        // quality
        var s_quality = et_quality.text.toString()
        if (s_quality.isBlank()) s_quality = "0"
        var quality = Integer.valueOf(s_quality)
        if (quality > 0) {
            item.cur_qual = quality
            item.orig_qual = quality
        }

        // weight
        var s_weight = et_weight.text.toString()
        if (s_weight.isBlank()) s_weight = "0"
        var f_weight = s_weight.toFloat()
        if (tv_weight_unit.text.toString() == "kg") {
            f_weight *= 1000
        }
        item.weight = Integer.valueOf(f_weight.roundToInt())

        // price
        var s_price = et_price.text.toString()
        if (s_price.isBlank()) s_price = "0"
        item.price = s_price.toFloat()

        if (!item.name.isBlank()) {
            c.viewModelScope.launch(Dispatchers.IO) {
                c.addToInventory(item)
                if (pay) {
                    c.moneyTransaction(
                        c.primaryAccount().nr,
                        0,
                        item.price,
                        getString(R.string.cinv_bought, item.name)
                    )
                }
            }
        } else {
            // TODO inform user that item was not created
        }
    }

    /**
     * switches weight units between g and kg - updates the input field
     */
    fun switchWeightUnit() {
        var cur_unit = tv_weight_unit.text.toString()
        var cur_weight = 0f
        var s_weight = ""
        var unit = ""

        s_weight = et_weight.text.toString()
        if (s_weight.isBlank()) s_weight = "0"

        when (cur_unit) {
            "g" -> {
                unit = "kg"
                cur_weight = s_weight.toFloat() / 1000
            }
            "kg" -> {
                unit = "g"
                cur_weight = s_weight.toFloat() * 1000
            }
        }
        tv_weight_unit.text = unit
        et_weight.setText(cur_weight.toString())
    }


    /**
     * Implements a [TextWatcher] to monitor the quality field
     */
    class TextChanged: TextWatcher {
        constructor(et: EditText) {
            this.et = et
        }
        private var et: EditText

        override fun afterTextChanged(p0: Editable?) {
            when (et.id) {
                R.id.newitem_quality -> {
                    var s_quality = et.text.toString()
                    if (s_quality.isBlank()) s_quality = "0"
                    if (Integer.valueOf(s_quality) > 12) {
                        s_quality = "12"
                        et.setText(s_quality)
                    }
                }
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

}