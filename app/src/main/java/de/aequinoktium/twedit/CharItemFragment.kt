package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
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
                         ItemQualDialog.DialogListener
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
        c.current_item = Item(c)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title = view.findViewById(R.id.char_item_name)
        tv_title.text = item.name

        tv_desc = view.findViewById(R.id.char_item_desc)
        tv_desc.text = item.desc

        tv_qty = view.findViewById(R.id.char_item_qty)
        var text = resources.getString(R.string.cinv_quantity) + " " + item.qty.toString()
        tv_qty.text = text

        tv_qual = view.findViewById(R.id.char_item_qual)
        var q = resources.getStringArray(R.array.cinv_qualities)
        text = resources.getString(R.string.cinv_quality) +
               " " + q[item.cur_qual] + " (" + item.cur_qual.toString() + ")"
        tv_qual.text = text
        tv_qual.setOnClickListener {editQual()}

        tv_price = view.findViewById(R.id.char_item_price)
        text = resources.getString(R.string.cinv_price) + " " + item.price.toString() + " IR"
        tv_price.text = text

        tv_weight = view.findViewById(R.id.char_item_weight)
        var s_wgt = " " + item.weight.toString() + " g"
        if (item.weight >= 1000) {
            s_wgt = " " + (item.weight.toFloat()/1000).toString() + " kg"
        }
        text = resources.getString(R.string.cinv_weight) + s_wgt
        tv_weight.text = text

        tv_dmg = view.findViewById(R.id.char_item_dmg)
        var dmg = item.dmg
        if (dmg.isBlank()) dmg = item.dmg_mod
        text = resources.getString(R.string.cinv_damage) + ": " + dmg
        if (dmg.isBlank()) {
            tv_dmg.visibility = View.GONE
        } else {
            tv_dmg.text = text
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
        bt_pack.setOnClickListener { pack() }
    }

    /**
     * Pack or unpack an item
     */
    fun pack() {
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

    /**
     * Equip or unequip an item
     */
    fun equip() {
        val is_equipped = item.equip()
        if (is_equipped == 1) {
            bt_equip.setText(R.string.cinv_drop)
        } else {
            bt_equip.setText(R.string.cinv_equip)
        }

    }

    /**
     * Implements the [DialogListener]
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is ItemPackDialog) { // pack item
            if (dialog.selected >= 0) {
                var cnt = dialog.containers.get(dialog.selected)
                item.packed_into = cnt.id
                item.equipped = 0
                bt_equip.setText(R.string.cinv_equip)
                bt_pack.setText(R.string.cinv_unpack)
                c.viewModelScope.launch(Dispatchers.IO) {
                    c.packItem(item)
                }
            }
        }
        if (dialog is ItemQualDialog) {
            val q = dialog.q
            var text = resources.getString(R.string.cinv_quality)
            val qualities = resources.getStringArray(R.array.cinv_qualities)
            text += "${qualities[q]} ($q)"
            tv_qual.text = text
            item.cur_qual = q
            c.viewModelScope.launch(Dispatchers.IO) {
                c.updateItem(item)
            }
        }
    }


}