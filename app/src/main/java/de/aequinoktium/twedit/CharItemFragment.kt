package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharItemFragment : Fragment(),
                         ItemPackDialog.DialogListener,
                         ItemQualDialog.DialogListener,
                         ItemSellDialog.DialogListener,
                         ItemQtyDialog.DialogListener,
                         ItemLoadClipDialog.DialogListener,
                         SettingsDialog.DialogListener
{
    private val c: CharacterViewModel by activityViewModels()
    lateinit var item: Item
    lateinit var tv_name: TextView
    lateinit var tv_desc: TextView
    lateinit var tv_qty: TextView
    lateinit var tv_qual: TextView
    lateinit var tv_price: TextView
    lateinit var tv_weight: TextView
    lateinit var tv_dmg: TextView
    lateinit var ll_actions: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(
            R.layout.fragment_char_item,
            container,
            false
        )

        initViews(root)

        item = c.current_item

        // clear current item so 'back' returns to full inventory.
        c.current_item = Item()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings = view.findViewById<ImageView>(R.id.char_item_settings)
        settings.setOnClickListener { settingsDialog() }
        setTexts()
        showActions()

    }

    fun initViews(view: View) {
        tv_name = view.findViewById(R.id.char_item_name)
        tv_desc = view.findViewById(R.id.char_item_desc)
        tv_qty = view.findViewById(R.id.char_item_qty)
        tv_qty.setOnClickListener {editQuantityDialog()}
        tv_qual = view.findViewById(R.id.char_item_qual)
        tv_qual.setOnClickListener {editQualityDialog()}
        tv_price = view.findViewById(R.id.char_item_price)
        tv_price.setOnClickListener {sellItemDialog()}
        tv_weight = view.findViewById(R.id.char_item_weight)
        tv_dmg = view.findViewById(R.id.char_item_dmg)
        ll_actions = view.findViewById(R.id.char_item_actions)
    }

    fun setTexts() {
        setNameAndDescText()
        setQuantityText()
        setQualityText()
        setWeightText()
        setPriceText()
        setDamageText()
    }

    fun setNameAndDescText() {
        tv_name.text = item.name
        val desc = item.desc
        tv_desc.text = addCaliberInfoToString(desc)
    }

    fun setQuantityText() {
        val qty_label = resources.getString(R.string.cinv_quantity)
        val qty_text = "$qty_label ${item.qty}"
        tv_qty.text = qty_text
    }

    fun setQualityText() {
        val qual_label = resources.getString(R.string.cinv_quality)
        val qualities = resources.getStringArray(R.array.cinv_qualities)
        val qual_text = "$qual_label ${qualities[item.cur_qual]} (${item.cur_qual})"
        tv_qual.text = qual_text
    }

    fun setWeightText() {
        var s_wgt = " " + item.weight.toString() + " g"
        if (item.weight >= 1000) {
            s_wgt = " " + (item.weight.toFloat()/1000).toString() + " kg"
        }
        val weight_label = resources.getString(R.string.cinv_weight)
        val weight_text =  "$weight_label $s_wgt"
        tv_weight.text = weight_text
    }

    fun setDamageText() {
        val dmg_label = resources.getString(R.string.cinv_damage)
        val dmg_text = "$dmg_label: ${item.cur_dmg}"
        if (item.cur_dmg.isEmpty()) {
            tv_dmg.visibility = View.GONE
        } else {
            tv_dmg.text = dmg_text
            tv_dmg.visibility = View.VISIBLE
        }
    }

    fun setPriceText() {
        val price_label = resources.getString(R.string.cinv_price)
        val price = resources.getString(R.string.cinv_money, item.price)
        val price_text =  "$price_label $price"
        tv_price.text = price_text
    }


    /**
     * Pack or unpack an item
     */
    fun packItemDialog() {
        val fm = this.parentFragmentManager
        val dialog = ItemPackDialog(item)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun editQualityDialog() {
        val fm = this.parentFragmentManager
        val dialog = ItemQualDialog(item.cur_qual)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun editQuantityDialog() {
        val fm = this.parentFragmentManager
        val dialog = ItemQtyDialog(item.qty)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun sellItemDialog() {
        val fm = this.parentFragmentManager
        val dialog = ItemSellDialog(item)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun loadClipDialog() {
        val fm = this.parentFragmentManager
        val dialog = ItemLoadClipDialog(item,"ammo")
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun insertClipDialog() {
        val fm = this.parentFragmentManager
        val dialog = ItemLoadClipDialog(item,"clipsnmore")
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun settingsDialog() {
        val fm = this.parentFragmentManager
        val settings = arrayOf(
            "inventory.check_weight_limit:Boolean",
            "inventory.check_caliber:Boolean"
        )
        val dialog = SettingsDialog(settings)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    fun equipItem() {
        if (item.equipped == 0) {
            item.equipped = 1
            if (item.cls == "clothing") {
                item.packed_into = -1
            } else if (item.cls.startsWith("weapon_")) {
                item.packed_into = -2
            } else {
                item.packed_into = 0
            }
        } else {
            item.equipped = 0
            item.packed_into = 0
        }
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            withContext(Dispatchers.Main) {
                showActions()
            }
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
        if (dialog is ItemLoadClipDialog) {
            val ammo = c.getItemById(dialog.selected_id)
            if (dialog.cls == "ammo") {
                loadIntoClip(ammo)
            } else {
                insertClip(ammo)
            }
        }
        if (dialog is SettingsDialog) {
            val settings: SettingsViewModel by activityViewModels()
            settings.update("inventory.check_weight_limit", dialog.values[0] as Boolean)
            settings.update("inventory.check_caliber", dialog.values[1] as Boolean)
        }
    }

    /**
     * pack the item into the specified container
     * @param cnt an [Item] which serves as container
     */
    fun pack(cnt: Item) {
        item.packed_into = cnt.id
        item.equipped = 0
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            withContext(Dispatchers.Main) {
                showActions()
            }
        }
    }

    fun unpackItem() {
        item.packed_into = 0
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            withContext(Dispatchers.Main) {
                showActions()
            }
        }
    }

    /**
     * set the item quality to the given value
     * @param q: Int quality in range 0-12
     */
    fun setQuality(q: Int) {
        item.cur_qual = q
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            withContext(Dispatchers.Main) {
                setQualityText()
            }
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
                    i.packed_into = 0
                    c.updateItem(i)
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


    /**
     * loads ammo into a clip
     * Ammo is only loaded if the item has has free capacity.
     * The ammo item is split into items of quantity 1.
     * @param ammo an [Item] that is used as ammo
     */
    fun loadIntoClip(ammo: Item) {
        var currently_loaded = 0
        for (i in c.getItemContents(item)) {
            currently_loaded += i.qty
        }
        val current_capacity = item.capacity - currently_loaded
        for (i in 1..current_capacity) {
            if (ammo.qty == 1) {
                ammo.packed_into = item.id
                c.viewModelScope.launch(Dispatchers.IO) { c.updateItem(ammo) }
            } else if (ammo.qty > 1) {
                ammo.qty -= 1
                val new_ammo = ammo.copy()
                new_ammo.qty = 1
                new_ammo.packed_into = item.id
                c.viewModelScope.launch(Dispatchers.IO) {
                    c.updateItem(ammo)
                    c.addToInventory(new_ammo)
                }
            }
        }
    }

    fun insertClip(ammo: Item) {
        if (item.clip > 0) {
            val cur_clip = c.getItemById(item.clip)
            cur_clip.packed_into = 0
            c.viewModelScope.launch(Dispatchers.IO) {
                c.updateItem(cur_clip)
            }
        }
        ammo.packed_into = item.id
        item.clip = ammo.id
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            c.updateItem(ammo)
            withContext(Dispatchers.Main) {
                showActions()
            }
        }
    }

    fun ejectClip() {
        if (item.clip > 0) {
            val cur_clip = c.getItemById(item.clip)
            cur_clip.packed_into = 0
            item.clip = 0
            c.viewModelScope.launch(Dispatchers.IO) {
                c.updateItem(cur_clip)
                c.updateItem(item)
                withContext(Dispatchers.Main) {
                    showActions()
                }
            }
        }
    }

    fun cycleGun() {
        val chambered = c.getChamberedAmmo(item).toMutableList()
        val clip = c.getItemById(item.clip)
        val clip_ammo = c.getItemContents(clip)

        if (chambered.size == item.chambers) {
            val loaded = chambered[0]
            loaded.packed_into = 0
            c.viewModelScope.launch(Dispatchers.IO) {
                c.updateItem(loaded)
            }
            chambered.removeAt(0)
        }
        if (clip_ammo.size > 0) {
            clip_ammo[0].packed_into = item.id
            c.viewModelScope.launch(Dispatchers.IO) {
                c.updateItem(clip_ammo[0])
            }
            chambered.add(clip_ammo[0])
        }
        item.chambered = arrayOf()
        for (i in chambered) {
            item.chambered += i.id
        }
        c.viewModelScope.launch(Dispatchers.IO) {
            c.updateItem(item)
            withContext(Dispatchers.Main) {
                item.cur_dmg = c.getItemEffectiveDamage(item)
                setDamageText()
            }
        }
    }

    fun addCaliberInfoToString(input: String):String {
        Log.d("info", "in addCaliberInfoToString")
        var result = ""
        if (!item.caliber[0].isEmpty() && !item.caliber[1].isEmpty()) {
            val weapons = mapOf(
                "pistol" to getString(R.string.cinv_cal_pistol),
                "rifle" to getString(R.string.cinv_cal_pistol),
                "shotgun" to getString(R.string.cinv_cal_pistol)
            )
            val caliber = mapOf(
                "light" to getString(R.string.cinv_cal_light),
                "medium" to getString(R.string.cinv_cal_medium),
                "heavy" to getString(R.string.cinv_cal_heavy)
            )
            result = "${weapons[item.caliber[0]]} - ${caliber[item.caliber[1]]}"
        }
        if (!input.isEmpty()) result = "\n" + result
        return input + result
    }


    /**
     * add item_specific actions to the layout
     */
    fun showActions() {
        ll_actions.removeAllViews()
        if (item.chambers > 0 && item.clip >= 0) {
            val iv = prepareIcon(R.drawable.action_cycle_gun)
            iv.setOnClickListener { cycleGun() }
        }

        if (item.cls == "clipsnmore" && item.capacity > 0 && !item.caliber[1].isEmpty()){
            val iv = prepareIcon(R.drawable.action_load_ammo)
            iv.setOnClickListener { loadClipDialog() }
        }
        if (item.clip == 0) {
            val iv = prepareIcon(R.drawable.action_load_clip)
            iv.setOnClickListener { insertClipDialog() }
        }
        if (item.clip > 0) {
            val iv = prepareIcon(R.drawable.action_unload_clip)
            iv.setOnClickListener { ejectClip() }
        }

        if (item.equipped == 0)  {
            val iv = prepareIcon(R.drawable.action_equip)
            iv.setOnClickListener { equipItem() }
        } else {
            val iv = prepareIcon(R.drawable.action_unequip)
            iv.setOnClickListener { equipItem() }
        }

        if (item.packed_into == 0) {
            val iv = prepareIcon(R.drawable.action_pack)
            iv.setOnClickListener { packItemDialog() }
        } else {
            val iv = prepareIcon(R.drawable.action_unpack)
            iv.setOnClickListener {unpackItem()}
        }

    }

    fun prepareIcon(id: Int): ImageView {
        val iv = ImageView(context)
        iv.setImageResource(id)
        val lp = LinearLayout.LayoutParams(px(48).toInt(),px(48).toInt())
        lp.setMargins(0,0,px(6).toInt(),px(6).toInt())
        iv.layoutParams = lp
        ll_actions.addView(iv)
        return iv
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}