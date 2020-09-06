package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController


class CharInventoryContainerFragment : Fragment(){
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var cnt: Item
    private lateinit var tv_title: TextView
    private lateinit var ll_container: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View

        root = inflater.inflate(
            R.layout.fragment_char_inventory_container,
            container,
            false
        )

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ll_container = view.findViewById(R.id.char_inv_container)
        tv_title = view.findViewById(R.id.char_inv_cont_title)

        if (c.current_item.id == 0) {
            tv_title.text = getString(R.string.cinv_all)
            for (item in c.getInventory()) {
                val iv = ItemView(context)
                iv.text = item.name
                iv.item = item
                iv.setOnClickListener { v -> editItem(v)}
                ll_container.addView(iv)
            }
        } else {
            cnt = c.current_item
            tv_title.text = cnt.name

            for (item in c.getInventory()) {
                if (item.packed_into == cnt.id) {
                    val iv = ItemView(context)
                    iv.text = item.name
                    iv.item = item
                    iv.setOnClickListener {v -> editItem(v)}
                    ll_container.addView(iv)
                }
            }

            // add self
            val iv = ItemView(context)
            iv.text = cnt.name
            iv.item = cnt
            iv.setOnClickListener {v -> editItem(v)}
            ll_container.addView(iv)


        }
    }

    fun editItem(view: View) {
        view as ItemView
        c.current_item = view.item
        this.findNavController().navigate(R.id.action_cinvcont_to_citem)
    }
}