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

        val bt = view.findViewById<Button>(R.id.test_button)
        bt.setOnClickListener { v -> clicked(v) }
    }


    fun clicked(v: View) {

        if (v is Button) {
            v.text
            var d = 0f
            for (i in 0..10000) {
                d += ewt.roll(1,9)[0] as Float
            }
            v.text = (d/9999).toString()
        }
    }

}