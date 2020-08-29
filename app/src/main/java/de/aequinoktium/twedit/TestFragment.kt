package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

/**
 * This fragment is just used for test purposes
 */
class TestFragment : Fragment() {
    val ewt = EWT()

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
        bt.setOnClickListener {v -> clicked(v)}
    }

    fun clicked(v: View) {
        if (v is Button) {
            v.text = "Clicked"
        }
    }

}