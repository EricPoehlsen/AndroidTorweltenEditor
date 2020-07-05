package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class MenuFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v: View = inflater.inflate(R.layout.fragment_menu, container, false)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity
        var m_cs = act.findViewById<Button>(R.id.menu_charselect)
        m_cs.setOnClickListener {
            this.findNavController().navigate(R.id.action_menu_to_cs)
        }
        var m_test = act.findViewById<Button>(R.id.menu_test)
        m_test.setOnClickListener {
            this.findNavController().navigate(R.id.action_menu_to_test)
        }

    }

}