package de.aequinoktium.twedit

import android.content.ContentValues
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CharInventoryNewFragment(private var container: String) : Fragment(){
    private val c: CharacterViewModel by activityViewModels()

    private lateinit var et_name: EditText
    private lateinit var et_desc: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root: View

        root = inflater.inflate(
            R.layout.fragment_char_inventory_new,
            container,
            false
        )

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_name = view.findViewById(R.id.newitem_name)
        et_desc = view.findViewById(R.id.newitem_desc)

        var bt_add = view.findViewById<Button>(R.id.newitem_add)
        bt_add.setOnClickListener {
            addItem()
        }


    }


    fun addItem() {
        var item = Item(c)

        item.name = et_name.text.toString()
        item.desc = et_desc.text.toString()

        c.viewModelScope.launch(Dispatchers.IO) {
            c.addToInventory(item)
        }
    }

}