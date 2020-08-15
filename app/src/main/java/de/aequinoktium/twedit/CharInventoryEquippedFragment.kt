package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController


/**
 * This [Fragment] is used to display the items the character
 * has currently equipped. Those are worn pieces of clothing,
 * bags and containers, tools and weapons.
 */
class CharInventoryEquippedFragment : Fragment(){
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
            R.layout.fragment_char_inventory_equipped,
            container,
            false
        )

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var ll = view.findViewById<LinearLayout>(R.id.cinv_equipped_container)

        for (item in c.getInventory()) {
            if (item.equipped == 1) {
                val iv = ItemView(context)
                iv.item = item
                iv.text = item.name
                ll.addView(iv)
            }
        }
    }

    fun editItem(view: View) {
        view as ItemView
        c.current_item = view.item
        this.findNavController().navigate(R.id.action_cinvcont_to_citem)


    }
}