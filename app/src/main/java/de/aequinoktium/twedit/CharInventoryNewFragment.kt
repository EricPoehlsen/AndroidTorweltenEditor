package de.aequinoktium.twedit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


class CharInventoryNewFragment : Fragment(),
                                 AdapterView.OnItemSelectedListener,
                                 ItemContainerDialog.DialogListener,
                                 ItemDamageDialog.DialogListener,
                                 ItemMaterialDialog.DialogListener,
                                 ItemColorDialog.DialogListener
{

    private val c: CharacterViewModel by activityViewModels()

    private lateinit var et_name: EditText
    private lateinit var et_desc: EditText
    private lateinit var et_weight: EditText
    private lateinit var et_price: EditText
    private lateinit var et_quality: EditText
    private lateinit var et_quantity: EditText
    private lateinit var tv_weight_unit: TextView
    private lateinit var sp_cls: Spinner
    private lateinit var bt_dmg: Button

    private var item_cls = "generic"
    private var item_cap = 0
    private var item_cont_name = ""
    private var item_dmg = ""


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
        et_quantity = view.findViewById(R.id.newitem_quantity)
        et_quantity.addTextChangedListener(TextChanged(et_quantity))
        et_quality = view.findViewById(R.id.newitem_quality)
        et_quality.addTextChangedListener(TextChanged(et_quality))
        et_price = view.findViewById(R.id.newitem_price)
        et_weight = view.findViewById(R.id.newitem_weight)
        tv_weight_unit = view.findViewById(R.id.newitem_weight_unit)
        tv_weight_unit.setOnClickListener{switchWeightUnit()}

        sp_cls = view.findViewById(R.id.newitem_cls)
        val adapter = ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item)

        val item_cls = arrayOf(
            getString(R.string.cinv_cls_generic),
            getString(R.string.cinv_cls_clothing),
            getString(R.string.cinv_cls_container),
            getString(R.string.cinv_cls_tool),
            getString(R.string.cinv_cls_weapon),
            getString(R.string.cinv_cls_ammo),
            getString(R.string.cinv_cls_implant)
        )
        for (i in item_cls) {
            adapter.add(i)
        }

        sp_cls.adapter = adapter
        sp_cls.onItemSelectedListener = this


        val bt_take = view.findViewById<Button>(R.id.newitem_take)
        bt_take.setOnClickListener {
            addItem()
        }
        val bt_buy = view.findViewById<Button>(R.id.newitem_buy)
        bt_buy.setOnClickListener {
            addItem(pay=true)
        }

        val bt_cont = view.findViewById<Button>(R.id.newitem_container)
        bt_cont.setOnClickListener { editContainer() }

        val bt_color = view.findViewById<Button>(R.id.newitem_color)
        bt_color.setOnClickListener { editColor() }

        val bt_material = view.findViewById<Button>(R.id.newitem_material)
        bt_material.setOnClickListener { editMaterial() }


        bt_dmg = view.findViewById(R.id.newitem_weapon)
        bt_dmg.setOnClickListener { editDamage() }

    }

    /**
     * prepares the item to be added to the character and
     * hands over item storage to [CharacterViewModel]
     */
    fun addItem(pay: Boolean = false) {
        val item = Item()

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
        val quality = Integer.valueOf(s_quality)
        if (quality > 0) {
            item.cur_qual = quality
            item.orig_qual = quality
        }

        // quantity
        var s_quantity = et_quantity.text.toString()
        if (s_quantity.isBlank()) s_quantity = "1"
        val qty = s_quantity.toInt()
        if (qty > 0) item.qty = qty

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

        // container:
        if (item_cap > 0) {
            item.weight_limit = item_cap
            item.container_name = item_cont_name
        }

        // damage
        if (!item_dmg.isBlank()) {
            if (item_dmg.startsWith("-") ||
                item_dmg.startsWith("±") ||
                item_dmg.startsWith("+")) {
                item.dmg_mod = item_dmg
            } else {
                item.dmg = item_dmg
            }
        }

        if (!item.name.isBlank()) {
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
        } else {
            // TODO inform user that item was not created
        }
    }

    fun editContainer() {
        val fm = this.parentFragmentManager
        val dialog = ItemContainerDialog(item_cap, item_cont_name)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun editDamage() {
        val fm = this.parentFragmentManager
        val dialog = ItemDamageDialog()
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun editColor() {
        val fm = this.parentFragmentManager
        val dialog = ItemColorDialog()
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun editMaterial() {
        val fm = this.parentFragmentManager
        val dialog = ItemMaterialDialog()
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    /**
     * switches weight units between g and kg - updates the input field
     */
    fun switchWeightUnit() {
        val cur_unit = tv_weight_unit.text.toString()
        var cur_weight = 0f
        var s_weight = et_weight.text.toString()
        var unit = ""


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
     * Implements a [AdapterView.OnItemSelectedListener]
     */

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    /**
     * updates the item class when the selection is made.
     */
    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (p2) {
            0 -> item_cls = "generic"
            1 -> item_cls = "clothing"
            2 -> item_cls = "container"
            3 -> item_cls = "tool"
            4 -> item_cls = "weapon"
            5 -> item_cls = "ammo"
            6 -> item_cls = "implant"
        }
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
                    var qual = s_quality.toInt()
                    if (qual < 1) qual = 1
                    if (qual > 12) qual = 12
                    et.setText(qual.toString())
                }
                R.id.newitem_quantity -> {
                    var s_quantity = et.text.toString()
                    if (s_quantity.isBlank()) s_quantity = "0"
                    val qty = s_quantity.toInt()
                    if (qty < 1) et.setText("1")
                }
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }

    /**
     * Handle the return of various dialogs
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is ItemContainerDialog) {
            setContainer(dialog.capacity, dialog.item_cont_name)
        }
        if (dialog is ItemDamageDialog) {
            val mod = dialog.cb_mod.isChecked
            setDamage(dialog.s, dialog.d, dialog.t, mod)
        }
        if (dialog is ItemColorDialog) {

        }
        if (dialog is ItemMaterialDialog) {

        }
    }

    fun setContainer(capacity: Int, name: String) {
        item_cap = capacity
        var cont_name = name
        cont_name = cont_name.replace(":", "")
        cont_name = cont_name.replace(",", "")
        cont_name = cont_name.replace("'", "\u2019")
        item_cont_name = cont_name
    }

    /**
     * construct the 'damage code' as string
     * set the variable and update the button
     * @param s: 'Schaden' number of damage dice
     * @param d: 'Durchschlag' [EWT] table column -7 .. 7
     * @param t: 'Typ' damage type P, E, M (empty = P)
     * @param mod: true if a damage modifier
     */
    fun setDamage(s: Int, d: Int, t: String="", mod: Boolean=false) {
        var dmg = ""
        if (mod) {
            if (s == 0) dmg += "±"
            if (s > 0) dmg += "+"
        }
        dmg += s.toString()
        dmg += "/"
        if (mod) {
            if (d == 0) dmg += "±"
            if (d > 0) dmg += "+"
        }
        dmg += d.toString()
        if (!t.isBlank()) {
            dmg += "/"
            dmg += t.toUpperCase()
        }
        if (s == 0 && d == 0) {
            dmg = ""
            bt_dmg.text = resources.getString(R.string.cinv_damage)
        } else {
            bt_dmg.text = dmg
        }
        item_dmg = dmg
    }
}