package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class CharInventoryFragment : Fragment(){
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var tv_cash: TextView

    private lateinit var rv_container: RecyclerView
    private lateinit var rv_adapter: RecyclerView.Adapter<*>
    private lateinit var rv_manager: RecyclerView.LayoutManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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


        // button: Add Item
        val bt_new = view.findViewById<Button>(R.id.cinv_new_item)
        bt_new.setOnClickListener {
            this.findNavController().navigate(R.id.action_cinv_to_cat)
        }

    }



    fun showItem(iv: ItemView) {
        c.current_item = iv.item
        this.findNavController().navigate(R.id.action_cinv_to_citem)
    }

    fun showContainer(view: View) {
        view as ItemView
        c.current_item = view.item
        this.findNavController().navigate(R.id.action_cinv_to_cinvcont)
    }



    class ItemAdapter(val full_inventory: Array<Item>, val frgm: CharInventoryFragment):
        RecyclerView.Adapter<ItemAdapter.ViewHolder>(),
        View.OnClickListener
    {
        var inventory = arrayOf<Item>()


        class ViewHolder(val iv: ItemView) : RecyclerView.ViewHolder(iv)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val iv = ItemView(parent.context)
            val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0)
            lp.setMargins(6,6,6,6)
            iv.layoutParams = lp
            iv.setOnClickListener(this)
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

        init {
            for (item in full_inventory) {
                inventory += item
            }
        }
    }


}