package de.aequinoktium.twedit

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.text.HtmlCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_char_inventory.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharInventoryFragment : Fragment(){
    private val c: CharacterViewModel by activityViewModels()
    private lateinit var ll_containers: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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

        ll_containers = view.findViewById(R.id.cinv_containers)

        val b_other = view.findViewById<ItemView>(R.id.cinv_other_items)
        b_other.item = Item(c)
        b_other.setOnClickListener { v -> showContainer(v) }

        val b_equipped = view.findViewById<Button>(R.id.cinv_equipped)
        b_equipped.setOnClickListener {
            this.findNavController().navigate(R.id.action_cinv_to_cinvequip)
        }
        displayEquippedContainers()




        // button: switch to inventory
        val b_inv = view.findViewById<Button>(R.id.cinv_new_item)
        b_inv.setOnClickListener {
            this.findNavController().navigate(R.id.action_cinv_to_cinvnew)
        }

    }

    fun displayEquippedContainers() {
        for (item in c.getInventory()) {
            if (item.equipped == 1 && item.weight_limit > 0) {

            }
        }
    }

    fun showContainer(view: View) {
        view as ItemView
        c.current_item = view.item
        this.findNavController().navigate(R.id.action_cinv_to_cinvcont)
    }
}