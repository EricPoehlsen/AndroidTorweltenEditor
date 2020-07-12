package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

/**
 * Displays the character traits and allows to modify them
 */
class CharTraitFragment : Fragment() {
    private val c: CharacterViewModel by activityViewModels()
    private var char_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_trait, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity

        // switch to trait selector fragment
        val b_add_skills = act.findViewById<Button>(R.id.chartraits_add)
        b_add_skills.setOnClickListener {
            this.findNavController().navigate(R.id.action_ct_to_ts)
        }
    }
}