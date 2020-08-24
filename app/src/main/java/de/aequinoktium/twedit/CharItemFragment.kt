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


class CharItemFragment : Fragment(), ItemPackDialog.DialogListener {
    private val c: CharacterViewModel by activityViewModels()
    lateinit var item: Item
    lateinit var tv_title: TextView
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

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title = view.findViewById(R.id.char_item_name)
        tv_title.text = item.name

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
                c.viewModelScope.launch(Dispatchers.IO) {
                    c.packItem(item)
                }
            }
        }
    }


}