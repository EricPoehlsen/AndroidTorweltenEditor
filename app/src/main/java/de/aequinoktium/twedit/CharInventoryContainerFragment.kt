package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController


class CharInventoryContainerFragment : Fragment(){
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var cnt: Item
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
        ll_container = view.findViewById(R.id.inv_container)
        if (c.current_item.id == 0) {

            for (item in c.getInventory()) {
                var tv = ItemView(context)
                tv.text = item.name
                tv.item = item
                tv.setOnClickListener {v -> editItem(v)}
                ll_container.addView(tv)
            }
        } else {
            cnt = c.current_item
            for (item in c.getInventory()) {
                if (item.packed_into == cnt.id) {
                    var iv = ItemView(context)
                    iv.text = item.name
                    iv.item = item
                    iv.setOnClickListener {v -> editItem(v)}
                    ll_container.addView(iv)
                }
            }

            // add self
            var iv = ItemView(context)
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