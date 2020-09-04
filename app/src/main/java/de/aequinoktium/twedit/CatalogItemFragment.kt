package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels


class CatalogItemFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private val d: DataViewModel by activityViewModels()
    private lateinit var catalog_item: CatalogItem


    private var view_ids = arrayOf<Int>()

    private lateinit var layout: ConstraintLayout
    private lateinit var tv_price: TextView
    private lateinit var tv_weight: TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        catalog_item = d.current_catalog_item

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(
            R.layout.fragment_catalog_item,
            container,
            false
        )
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout = view as ConstraintLayout

        val tv_title = view.findViewById<TextView>(R.id.catalog_item_name)
        tv_title.text = catalog_item.name

        tv_weight = view.findViewById(R.id.catalog_item_weight)
        tv_weight.text = weightText()
        tv_price = view.findViewById(R.id.catalog_item_price)

        for (name in catalog_item.variants.keys) {
            addVariant(name)
        }

        updateLayout()




    }


    fun addVariant(name: String) {
        val tv = TextView(context)
        tv.text = name
        tv.id = View.generateViewId()
        layout.addView(tv)
        view_ids += tv.id


        val sp = Spinner(context)

        val names = ArrayList<String>()
        for (variant in catalog_item.variants[name]!!) {
            names.add(variant.name)
        }
        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            names
        )
        sp.adapter = adapter

        sp.onItemSelectedListener = SelectListener(name,this)

        sp.id = View.generateViewId()
        layout.addView(sp)
        view_ids += sp.id
    }

    fun updateLayout() {
        val constraint_set = ConstraintSet()
        constraint_set.clone(layout)
        view_ids += R.id.catalog_item_buy

        var first = true
        for (i in 0..view_ids.size - 2) {
            constraint_set.connect(
                view_ids[i],
                ConstraintSet.BOTTOM,
                view_ids[i+1],
                ConstraintSet.TOP
            )
            constraint_set.connect(
                view_ids[i],
                ConstraintSet.LEFT,
                layout.id,
                ConstraintSet.LEFT
            )
        }
        layout.setConstraintSet(constraint_set)
    }

    fun weightText(): String {
        val label = getString(R.string.cinv_weight)
        var weight = calcWeight().toFloat()
        var unit = "g"
        if (weight > 1000) {
            weight = weight/1000
            unit = "kg"
        }

        return "${label} ${weight}${unit}"
    }


    fun calcWeight(): Int {
        var weight = catalog_item.weight
        for (all in catalog_item.variants.values)
            for (variant in all) {
                if (variant.selected) weight = (weight * variant.weight_factor).toInt()
            }
        return weight
    }

    fun update() {
        tv_weight.text = weightText()
    }


    class SelectListener(val name:String, val frgm: CatalogItemFragment): AdapterView.OnItemSelectedListener {
        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selected: Int, p3: Long) {
            val variants = frgm.catalog_item.variants[name]
            for (variant in variants!!) {
                variant.selected = false
            }
            variants[selected].selected = true
            frgm.update()
        }

        // unused - necessary for implementation
        override fun onNothingSelected(p0: AdapterView<*>?) {}


    }


}