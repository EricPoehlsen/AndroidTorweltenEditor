package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

/**
 * This fragment is just used for test purposes
 */
class TestFragment : Fragment() {
    val ewt = EWT()

    lateinit var iv1: ItemView
    lateinit var iv2: ItemView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val ll = view.findViewById<LinearLayout>(R.id.test_linear)
        addItemView(ll)
    }

    fun addItemView(ll: LinearLayout) {
        val item = Item()
        item.name = "Gegenstand"
        item.dmg.s = 1
        val iv = ItemView(context)
        iv.item = item
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0)
        lp.setMargins(6,6,6,6)
        iv.setPadding(6,6,6,6)
        iv.layoutParams = lp
        ll.addView(iv)
    }

    fun clicked(v: View) {

        if (v is Button) {
            v.text = ewt.roll(3,-2)[0].toString()
        }
    }

}