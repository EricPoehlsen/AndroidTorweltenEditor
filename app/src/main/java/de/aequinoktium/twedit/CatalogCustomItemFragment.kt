package de.aequinoktium.twedit

import android.graphics.Color
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


class CatalogCustomItemFragment : Fragment(),
                                 AdapterView.OnItemSelectedListener,
                                 ItemContainerDialog.DialogListener,
                                 ItemDamageDialog.DialogListener,
                                 ItemMaterialDialog.DialogListener,
                                 ItemColorDialog.DialogListener
{

    private val c: CharacterViewModel by activityViewModels()

    private val item = Item()

    private lateinit var tv_weight_unit: TextView
    private lateinit var sp_cls: Spinner
    private lateinit var bt_dmg: Button
    private lateinit var bt_color: Button
    private lateinit var et_weight: EditText


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

        val view_ids = arrayOf(
            R.id.newitem_name,
            R.id.newitem_desc,
            R.id.newitem_quantity,
            R.id.newitem_quality,
            R.id.newitem_price,
            R.id.newitem_weight
        )
        for (id in view_ids) {
            val et: EditText = view.findViewById(id)
            et.addTextChangedListener(TextChanged(et, this))
        }

        et_weight = view.findViewById(R.id.newitem_weight)


        tv_weight_unit = view.findViewById(R.id.newitem_weight_unit)
        tv_weight_unit.setOnClickListener{switchWeightUnit()}

        sp_cls = view.findViewById(R.id.newitem_cls)
        val adapter = ArrayAdapter<String>(view.context, R.layout.support_simple_spinner_dropdown_item)

        val item_cls = arrayOf(
            getString(R.string.cinv_cls_generic),
            getString(R.string.cinv_cls_valuable),
            getString(R.string.cinv_cls_clothing),
            getString(R.string.cinv_cls_container),
            getString(R.string.cinv_cls_tool),
            getString(R.string.cinv_cls_weapon),
            getString(R.string.cinv_cls_clipsnmore),
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

        bt_color = view.findViewById(R.id.newitem_color)
        bt_color.setOnClickListener { editColor() }

        val bt_material = view.findViewById<Button>(R.id.newitem_material)
        bt_material.setOnClickListener { editMaterial() }


        bt_dmg = view.findViewById(R.id.newitem_weapon)
        bt_dmg.setOnClickListener { editDamage() }

    }

    // set item name (just strip ' due to sql)
    fun setName(et: EditText) {
        var name = et.text.toString()
        name = name.replace("'", "\u2019")
        item.name = name
    }

    // set item description (just strip ' due to sql)
    fun setDescription(et: EditText) {
        var desc = et.text.toString()
        desc = desc.replace("'", "\u2019")
        item.desc = desc
    }

    // set item quality valid values int 1-12
    fun setQuality(et: EditText) {
        var s_quality = et.text.toString()
        if (s_quality.isBlank()) s_quality = "0"
        var quality = s_quality.toInt()
        if (quality < 1) {
            quality = 1
            et.setText(quality.toString())
        } else if (quality > 12) {
            quality = 12
            et.setText(quality.toString())
        }
        item.cur_qual = quality
        item.orig_qual = quality
    }

    // set item quantity valid value int >= 1
    fun setQuantity(et: EditText) {
        var s_quantity = et.text.toString()
        if (s_quantity.isBlank()) s_quantity = "0"
        var quantity = s_quantity.toInt()
        if (quantity < 1) {
            quantity = 1
            et.setText(quantity.toString())
        }
        item.qty = quantity
    }

    // set item weight in grams valid units int >= 0
    fun setWeight(et: EditText) {
        var s_weight = et.text.toString()
        if (s_weight.isBlank()) s_weight = "0"
        var f_weight = s_weight.toFloat()
        if (tv_weight_unit.text.toString() == "kg") {
            f_weight *= 1000
        }
        item.weight = Integer.valueOf(f_weight.roundToInt())
    }

    // set item price in rand as float >= 0f
    fun setPrice(et: EditText) {
        var s_price = et.text.toString()
        if (s_price.isBlank()) s_price = "0"
        item.price = s_price.toFloat()
    }

    // set container data
    fun setContainer(capacity: Int, name: String) {
        item.weight_limit = capacity
        var cont_name = name
        cont_name = cont_name.replace(":", "")
        cont_name = cont_name.replace(",", "")
        cont_name = cont_name.replace("'", "\u2019")
        item.container_name = cont_name
    }

    // set the color
    fun setColor(color: Int, color_name: String) {
        item.color = "$color.$color_name"
        bt_color.getBackground().setTint(color)
    }

    /**
     * prepares the item to be added to the character and
     * hands over item storage to [CharacterViewModel]
     */
    fun addItem(pay: Boolean = false) {
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
        val dialog = ItemContainerDialog(item.weight_limit, item.container_name)
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
     * switches weight units between g and kg - updates the weight
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
            0 -> item.cls = "generic"
            1 -> item.cls = "valuable"
            2 -> item.cls = "clothing"
            3 -> item.cls = "container"
            4 -> item.cls = "tool"
            5 -> item.cls = "weapon"
            6 -> item.cls = "clipsnmore"
            7 -> item.cls = "ammo"
            8 -> item.cls = "implant"
        }
    }



    /**
     * Handle the return of various dialogs
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is ItemContainerDialog) {
            setContainer(dialog.capacity, dialog.item_cont_name)
        }
        if (dialog is ItemDamageDialog) {
            setDamage(dialog.s, dialog.d, dialog.t, dialog.cb_mod.isChecked)
        }
        if (dialog is ItemColorDialog) {
            val col_array = arrayOf(dialog.cv.h,dialog.cv.s,dialog.cv.v).toFloatArray()
            val color = Color.HSVToColor(col_array)
            val color_name = dialog.cv.name
            setColor(color, color_name)
        }
        if (dialog is ItemMaterialDialog) {

        }
    }

    /** set damage and update button
     * @param s: 'Schaden' number of damage dice
     * @param d: 'Durchschlag' [EWT] table column -7 .. 7
     * @param t: 'Typ' damage type P, E, M (empty = P)
     */
    fun setDamage(s: Int, d: Int, t: String="", mod: Boolean) {
        val dmg = Damage(s,d,t,mod)
        if (s == 0 && d == 0) { // no value
            bt_dmg.text = resources.getString(R.string.cinv_damage)
        } else {
            bt_dmg.text = dmg.toString()
        }
        item.dmg = dmg
    }

    /**
     * Implements a [TextWatcher] to monitor the various EditTexts
     */
    class TextChanged(val et: EditText, val item: CatalogCustomItemFragment): TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
            when (et.id) {
                R.id.newitem_name -> item.setName(et)
                R.id.newitem_desc -> item.setDescription(et)
                R.id.newitem_quality -> item.setQuality(et)
                R.id.newitem_quantity -> item.setQuantity(et)
                R.id.newitem_price -> item.setPrice(et)
                R.id.newitem_weight -> item.setWeight(et)
            }
        }

        // unused ... necessary for implementation
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    }



}