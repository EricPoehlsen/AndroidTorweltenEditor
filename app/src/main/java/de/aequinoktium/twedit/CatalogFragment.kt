package de.aequinoktium.twedit

import android.os.Bundle
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

    private lateinit var ll_list: LinearLayout
    private val cls = "clothing"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ll_list = view.findViewById(R.id.catalog_itemlist)
        d.viewModelScope.launch(Dispatchers.IO) {
            d.loadCatalog(cls)
            withContext(Dispatchers.Main){
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

    fun showItem(item: CatalogItem) {
        d.current_catalog_item = item
        this.findNavController().navigate(R.id.action_cat_to_item)
    }
}