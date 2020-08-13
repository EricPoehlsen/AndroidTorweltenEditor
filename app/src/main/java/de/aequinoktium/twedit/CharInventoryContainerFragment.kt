package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController


class CharInventoryContainerFragment : Fragment(){
    private val c: CharacterViewModel by activityViewModels()


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
        view as LinearLayout
        if (c.current_item.id == 0) {

            for (item in c.getInventory()) {
                var tv = ItemView(context)
                tv.text = item.name
                tv.item = item
                tv.setOnClickListener {v -> editItem(v)}
                view.addView(tv)
            }

        }
    }

    fun editItem(view: View) {
        view as ItemView
        c.current_item = view.item
        this.findNavController().navigate(R.id.action_cinvcont_to_citem)


    }
}