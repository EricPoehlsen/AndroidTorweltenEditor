package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CharInventoryFragment : Fragment(),
    SettingsDialog.DialogListener
{
    private val c: CharacterViewModel by activityViewModels()
    private val settings: SettingsViewModel by activityViewModels()

    private var show_packed = false
    private var show_equipped = false
    private var empty_damage = false

    private lateinit var tv_cash: TextView
    private lateinit var rv_container: RecyclerView
    private lateinit var rv_adapter: ItemAdapter
    private lateinit var rv_manager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        show_packed = settings.getString("inventory.show_packed") == "1"
        show_equipped = settings.getString("inventory.show_equipped") == "1"

        val root: View

        root = inflater.inflate(
            R.layout.fragment_char_inventory,
            container,
            false
        )

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv_container = view.findViewById(R.id.cinv_container)
        rv_adapter = ItemAdapter(c.getInventory(), this)
        rv_manager = LinearLayoutManager(view.context)

        rv_container.layoutManager = rv_manager
        rv_container.adapter = rv_adapter

        tv_cash = view.findViewById(R.id.cinv_cash)
        tv_cash.setText(getString(R.string.cinv_cash, c.primaryAccount().balance))


        /* this.findNavController().navigate(R.id.action_cinv_to_cinvequip)*/

        val bt_all = view.findViewById<Button>(R.id.cinv_all)
        bt_all.setOnClickListener{rv_adapter.showAll()}

        // button: Add Item
        val bt_new = view.findViewById<Button>(R.id.cinv_new_item)
        bt_new.setOnClickListener {
            this.findNavController().navigate(R.id.action_cinv_to_cat)
        }

        val iv_settings = view.findViewById<ImageView>(R.id.cinv_settings)
        iv_settings.setOnClickListener{settings()}

    }



    fun showItem(iv: ItemView) {
        c.current_item = iv.item
        this.findNavController().navigate(R.id.action_cinv_to_citem)
    }


    fun settings() {
        val fm = this.parentFragmentManager
        val names = arrayOf(
            "inventory.show_equipped:Boolean",
            "inventory.show_packed:Boolean"
        )
        val dialog = SettingsDialog(names)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    /**
     * implements the DialogListener
     * updates the show_packed and show_equipped settings
     */
    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is SettingsDialog) {
            show_equipped = settings.update("inventory.show_equipped", dialog.values[0] as Boolean)
            show_packed = settings.update("inventory.show_packed", dialog.values[1] as Boolean)
            rv_adapter.showBasedOnSettings()
        }
    }

    /**
     * Adapter Class for the Recycler View
     */
    class ItemAdapter(val full_inventory: Array<Item>, val frgm: CharInventoryFragment):
        RecyclerView.Adapter<ItemAdapter.ViewHolder>(),
        View.OnClickListener,
        View.OnLongClickListener
    {
        var inventory = arrayOf<Item>()


        class ViewHolder(val iv: ItemView) : RecyclerView.ViewHolder(iv)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val iv = ItemView(parent.context)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0)
            lp.setMargins(6,6,6,6)
            iv.layoutParams = lp
            iv.setOnClickListener(this)
            iv.setOnLongClickListener(this)
            return ViewHolder(iv)
        }

        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            holder.iv.item = inventory[pos]
        }

        override fun getItemCount(): Int = inventory.size

        override fun onClick(view: View?) {
            if (view is ItemView) {
                frgm.showItem(view)
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (view is ItemView) {
                showContents(view.item)
            }
            return true
        }

        fun metaItems(): Array<Item> {
            var result = arrayOf<Item>()
            var show_clothing = false
            var show_weapons = false

            val clothing = Item().apply {
                id = -1
                name = frgm.getString(R.string.cinv_equipped_clothing)
            }
            val weapons = Item().apply {
                id = -2
                name = frgm.getString(R.string.cinv_equipped_weapons)
            }

            for (item in full_inventory) {
                if (item.packed_into == -1) show_clothing = true
                if (item.packed_into == -2) show_weapons = true
            }
            if (show_clothing) result += clothing
            if (show_weapons) result += weapons
            return result
        }

        fun showContents(item: Item) {
            inventory = arrayOf(item)
            for (i in full_inventory) {
                if (i.packed_into == item.id) {
                    inventory += i
                }
            }
            notifyDataSetChanged()
        }

        fun showAll() {
            inventory = arrayOf()
            for (item in full_inventory) {
                inventory += item
            }
            notifyDataSetChanged()
        }

        fun showEquipped() {
            inventory = arrayOf()
            for (item in full_inventory) {
                if (item.equipped == 1) {
                    inventory += item
                }
            }
            notifyDataSetChanged()
        }

        fun showUnpackedItems() {
            inventory = metaItems()
            for (item in full_inventory) {
                if (item.packed_into == 0) {
                    inventory += item
                }
            }
            notifyDataSetChanged()
        }

        fun showBasedOnSettings() {
            if (frgm.show_packed) {
                showAll()
            } else if (frgm.show_equipped) {
                showEquipped()
            } else {
                showUnpackedItems()
            }
        }

        init {
            if (frgm.c.current_item.id == 0) {
                showBasedOnSettings()
            } else {
                showContents(frgm.c.current_item)
            }

        }
    }
}