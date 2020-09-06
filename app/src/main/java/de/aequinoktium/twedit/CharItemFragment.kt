package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CharItemFragment : Fragment(),
                         ItemPackDialog.DialogListener,
                         ItemQualDialog.DialogListener,
                         ItemSellDialog.DialogListener,
                         ItemQtyDialog.DialogListener
{
    private val c: CharacterViewModel by activityViewModels()
    lateinit var item: Item
    lateinit var tv_title: TextView
    lateinit var tv_desc: TextView
    lateinit var tv_qty: TextView
    lateinit var tv_qual: TextView
    lateinit var tv_price: TextView
    lateinit var tv_weight: TextView
    lateinit var tv_dmg: TextView

    lateinit var bt_equip: Button
    lateinit var bt_pack: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View

        root = inflater.inflate(
            R.layout.fragment_char_item,
            container,
            false
        )

        item = c.current_item

        // clear current item so 'back' returns to full inventory.
        c.current_item = Item()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title = view.findViewById(R.id.char_item_name)
        tv_title.text = item.name

        tv_desc = view.findViewById(R.id.char_item_desc)
        tv_desc.text = item.desc

        tv_qty = view.findViewById(R.id.char_item_qty)
        val qty_label = resources.getString(R.string.cinv_quantity)
        val qty_text = "$qty_label ${item.qty}"
        tv_qty.text = qty_text
        tv_qty.setOnClickListener {editQty()}

        tv_qual = view.findViewById(R.id.char_item_qual)
        val qual_label = resources.getString(R.string.cinv_quality)
        val qualities = resources.getStringArray(R.array.cinv_qualities)
        val qual_text = "$qual_label ${qualities[item.cur_qual]} (${item.cur_qual})"
        tv_qual.text = qual_text
        tv_qual.setOnClickListener {editQual()}

        tv_price = view.findViewById(R.id.char_item_price)
        val price_label = resources.getString(R.string.cinv_price)
        val price = resources.getString(R.string.cinv_money, item.price)
        val price_text =  "$price_label $price"
        tv_price.text = price_text
        tv_price.setOnClickListener {sellItem()}

        tv_weight = view.findViewById(R.id.char_item_weight)
        var s_wgt = " " + item.weight.toString() + " g"
        if (item.weight >= 1000) {
            s_wgt = " " + (item.weight.toFloat()/1000).toString() + " kg"
        }
        val weight_label = resources.getString(R.string.cinv_weight)
        val weight_text =  "$weight_label $s_wgt"
        tv_weight.text = weight_text

        tv_dmg = view.findViewById(R.id.char_item_dmg)
        var dmg = item.dmg
        if (dmg.isBlank()) dmg = item.dmg_mod
        val dmg_label = resources.getString(R.string.cinv_damage)
        val dmg_text = "$dmg_label: $dmg"
        if (dmg.isBlank()) {
            tv_dmg.visibility = View.GONE
        } else {
            tv_dmg.text = dmg_text
        }

        bt_equip = view.findViewById(R.id.char_item_equip)
        if (item.equipped == 1) {
            bt_equip.setText(R.string.cinv_drop)
        }
        bt_equip.setOnClickListener { equip() }

        bt_pack = view.findViewById(R.id.char_item_pack)
        if (item.packed_into > 0) {
            bt_pack.setText(resources.getString(R.string.cinv_unpack))
        }
        bt_pack.setOnClickListener { packItem() }
    }

    /**
     * Pack or unpack an item
     */
    fun packItem() {
        if (item.packed_into > 0) { // unpack item
            item.packed_into = 0
            bt_pack.setText(R.string.cinv_pack)
            c.viewModelScope.launch(Dispatchers.IO) {
                c.unpackItem(item)
            }
        } else { // display pack item dialog
            val fm = this.parentFragmentManager
            val dialog = ItemPackDialog(item, c)
            dialog.setTargetFragment(this, 301)
            dialog.show(fm, null)
        }
    }

    fun editQual() {
        val fm = this.parentFragmentManager
        val dialog = ItemQualDialog(item.cur_qual)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun editQty() {
        val fm = this.parentFragmentManager
        val dialog = ItemQtyDialog(item.qty)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun sellItem() {
        val fm = this.parentFragmentManager
        val dialog = ItemSellDialog(item)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }


    /**
     * Equip or unequip an item
     */
    fun equip() {
        val is_equipped = c.equipItem(item)
        if (is_equipped == 1) {
            bt_equip.setText(R.string.cinv_drop)
        } else {
            bt_equip.setText(R.string.cinv_equip)
        }

    }

    /**
     * Implements the [DialogListener] for multiple dialogs
     * and calls the appropriate methods.
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is ItemPackDialog) { // pack item
            if (dialog.selected >= 0) {
                val cnt = dialog.containers.get(dialog.selected)
                pack(cnt)
            }
        }
        if (dialog is ItemQualDialog) { // change quality
            setQuality(dialog.q)
        }
        if (dialog is ItemSellDialog) { // sell item
            val p = dialog.p * item.qty
            sell(p)
        }
        if (dialog is ItemQtyDialog) {
            changeQuantity(dialog.action, dialog.qty)
        }
    }

    /**
     * pack the item into the specified container
     * @param cnt an [Item] which serves as container
     */
    fun pack(cnt: Item) {
        item.packed_into = cnt.id
        item.equipped = 0
        bt_equip.setText(R.string.cinv_equip)
        bt_pack.setText(R.string.cinv_unpack)
        c.viewModelScope.launch(Dispatchers.IO) {
            c.packItem(item)
        }
    }

    /**
     * set the item quality to the given value
     * @param q: Int quality in range 0-12
     */
    fun setQuality(q: Int) {
        var text = resources.getString(R.string.cinv_quality)
        val qualities = resources.getStringArray(R.array.cinv_qualities)
        text += "${qualities[q]} ($q)"
        tv_qual.text = text
        item.cur_qual = q
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
        }
    }

    /**
     * sell the item for the given price
     * @param price: total price to sell the item(stack)
     */
    fun sell(price: Float) {
        // unpack all items packed into
        for (i in c.getInventory()) {
            if (i.packed_into == item.id) {
                c.viewModelScope.launch(Dispatchers.IO) {
                    c.unpackItem(i)
                }
            }
        }
        // sell item
        c.viewModelScope.launch(Dispatchers.IO) {
            val purpose = resources.getString(R.string.cinv_sold, item.qty, item.name)
            c.moneyTransaction(0, c.primaryAccount().nr, price, purpose)
            c.removeItem(item)
        }
    }

    /**
     * handles changes in quantity, like buying or taking additional items
     * split the stack or joining the item with 'similar' items
     */
    fun changeQuantity(action: String, qty: Int) {
        when (action) {
            "buy" -> buy(qty, item.price)
            "take" -> buy(qty, 0f)
            "split" -> split(qty)
            "part" -> part()
            "join" -> join()
        }
    }

    /**
     * Buy additional items and add them to the stack
     * @param qty: Integer number of items to buy
     * @param price: Float price per unit
     */
    fun buy(qty: Int, price: Float) {
        item.qty += qty
        val text = resources.getString(R.string.cinv_quantity) + " " + item.qty.toString()
        tv_qty.text = text
        c.viewModelScope.launch(Dispatchers.IO) {
            val purpose = resources.getString(R.string.cinv_bought, item.qty, item.name)
            c.moneyTransaction(c.primaryAccount().nr, 0, price * qty, purpose)
            c.updateItem(item)
        }
    }

    /**
     * Split an item stack into two ...
     *
     */
    fun split(qty: Int) {
        if (qty in 1..item.qty-1) {
            val new_item = item.copy()
            item.qty = item.qty - qty
            val text = resources.getString(R.string.cinv_quantity) + " " + item.qty.toString()
            tv_qty.text = text
            new_item.qty = qty
            c.viewModelScope.launch(Dispatchers.IO) {
                c.updateItem(item)
                c.addToInventory(new_item)
            }
        }
    }

    /**
     * part an item stack into items with quantity one
     */
    fun part() {
        var new_items = arrayOf<Item>()
        repeat(item.qty-1) {
            val new_item = item.copy()
            new_item.qty = 1
            new_items += new_item
        }
        item.qty = 1
        val text = resources.getString(R.string.cinv_quantity) + " " + item.qty.toString()
        tv_qty.text = text
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            for (new_item in new_items) {
                c.addToInventory(new_item)
            }
        }
    }

    /**
     * join all similar items
     */
    fun join() {
        var equal_items = arrayOf<Item>()
        for (i in c.getInventory()) {
            if (i.id != item.id) {
                if (i.eq(item)) {
                    equal_items += i
                    item.qty += i.qty
                }
            }
        }
        val text = resources.getString(R.string.cinv_quantity) + " " + item.qty.toString()
        tv_qty.text = text
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            for (i in equal_items) {
                c.removeItem(i)
            }
        }
    }
}