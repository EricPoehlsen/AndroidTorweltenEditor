package de.aequinoktium.twedit

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CatalogFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private val d: DataViewModel by activityViewModels()

    private lateinit var sc_container: HorizontalScrollView
    private lateinit var ll_list: LinearLayout
    private var cls = ""

    private lateinit var tab_icons: Array<ImageView>

    private val item_classes = arrayOf(
        "clothing",
        "container",
        "tool",
        "weapon",
        "ammo",
        "clipsnmore",
        "generic",
        "valuable",
        "more"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(
            R.layout.fragment_catalog,
            container,
            false
        )
        ll_list = root.findViewById(R.id.catalog_itemlist)
        sc_container = root.findViewById(R.id.catalog_tab_scroll)
        sc_container.post { setScrollPosition() }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cls = d.current_catalog_class
        setupTabIcons(view)
        setTabStateFromClass()
        loadCatalog()
    }

    /**
     * Set
     */
    fun setupTabIcons(view: View) {
        tab_icons = arrayOf()
        val view_ids = arrayOf(
            R.id.catalog_icon_clothing,
            R.id.catalog_icon_container,
            R.id.catalog_icon_tools,
            R.id.catalog_icon_weapons,
            R.id.catalog_icon_ammo,
            R.id.catalog_icon_clipsnmore,
            R.id.catalog_icon_generic,
            R.id.catalog_icon_valuable,
            R.id.catalog_icon_more
        )
        for (id in view_ids) {
            val icon = view.findViewById<ImageView>(id)
            icon.setOnClickListener { v -> selectClass(tabClicked(v))}
            tab_icons += icon
        }
    }

    fun loadCatalog() {
        d.viewModelScope.launch(Dispatchers.IO) {
            d.loadCatalog(cls)
            withContext(Dispatchers.Main) {
                displayItems()
            }
        }
    }

    fun displayItems() {
        ll_list.removeAllViews()
        for (item in d.current_catalog) {
            val tv = TextView(context)
            tv.text = item.name
            tv.setOnClickListener{ _ -> showItem(item) }

            ll_list.addView(tv)
       }
    }

    /**
     * Updates the tabs when one was clicked
     * @param view the clicked tab
     * @return the position of the tab
     */
    fun tabClicked(view: View): Int {
        var i = 0
        var pos = 0
        for (icon in tab_icons) {
            if (icon == view) {
                icon.setBackgroundResource(R.drawable.icon_tab)
                pos = i
            } else {
                icon.setBackgroundResource(R.drawable.icon_tab_dark)
            }
            i++
        }
        return pos
    }

    fun setTabStateFromClass() {
        val pos = item_classes.indexOf(cls)
        var i = 0
        for (icon in tab_icons) {
            if (i == pos) {
                icon.setBackgroundResource(R.drawable.icon_tab)
            } else {
                icon.setBackgroundResource(R.drawable.icon_tab_dark)
            }
            i++
        }
    }

    /**
     * select the item class based on tab position -> loads catalog
     */
    fun selectClass(pos: Int) {
        cls = item_classes[pos]
        if (cls != "more") {
            loadCatalog()
            d.current_catalog_class = cls
        } else {
            newItem()
            d.current_catalog_class = "clothing"
        }

    }

    fun showItem(item: CatalogItem) {
        d.current_catalog_item = item
        this.findNavController().navigate(R.id.action_cat_to_item)
    }

    fun newItem() {
        this.findNavController().navigate(R.id.action_cat_to_cinvnew)
    }

    fun setScrollPosition() {
        val x = px(48).toInt() * item_classes.indexOf(cls)
        Log.d("info", "X: $x")
        sc_container.smoothScrollTo(x,0)
    }

    // calculate px for dp value
    fun px(dp: Int): Float = dp * resources.displayMetrics.density
}