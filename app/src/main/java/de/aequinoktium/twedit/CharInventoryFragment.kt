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
    CharInventorySettingsDialog.DialogListener
{
    private val c: CharacterViewModel by activityViewModels()
    private val settings: SettingsViewModel by activityViewModels()

    private var show_packed = false
    private var show_equipped = false



    private lateinit var tv_cash: TextView
    private lateinit var rv_container: RecyclerView
    private lateinit var rv_adapter: ItemAdapter
    private lateinit var rv_manager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        show_packed = settings.find("inventory.show_packed") == "1"
        show_equipped = settings.find("inventory.show_packed") == "1"

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
        val dialog = CharInventorySettingsDialog(show_packed, show_equipped)
        dialog.setTargetFragment(this, 301)
        dialog.show(fm, null)
    }

    override fun onDialogPositiveClick(dialog: DialogFragment) {
        if (dialog is CharInventorySettingsDialog) {
            Log.d("info", "equipped: ${dialog.equipped}, packed: ${dialog.packed}, ")

            show_equipped = settings.update("inventory.show_equipped", dialog.equipped)
            show_packed = settings.update("inventory.show_packed", dialog.packed)
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
                inventory = arrayOf(view.item)
                for (item in full_inventory) {
                    if (item.packed_into == view.item.id) {
                        inventory += item
                    }
                }
                notifyDataSetChanged()
            }

            Log.d("info", "LONG CLICK!")
            return true
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

        fun showUnpacked() {
            inventory = arrayOf()
            for (item in full_inventory) {
                if (item.packed_into == 0) {
                    inventory += item
                }
            }
            notifyDataSetChanged()
        }

        init {
            showUnpacked()
        }
    }




}