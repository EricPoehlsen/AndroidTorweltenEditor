package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

/**
 * Displays the character traits and allows to modify them
 */
class CharTraitFragment : Fragment() {
    var char_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            char_id = it.getInt("char_id", 0)
        }
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

        var act = activity as MainActivity

        // switch to trait selector fragment
        var b_add_skills = act.findViewById<Button>(R.id.chartraits_add)
        b_add_skills.setOnClickListener {
            val bundle: Bundle = bundleOf("char_id" to char_id)
            this.findNavController().navigate(R.id.action_ct_to_ts, bundle)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param char_id the character id
         * @return A new instance of fragment CharTraitFragment.
         */
        @JvmStatic
        fun newInstance(char_id: Int) =
            CharTraitFragment().apply {
                arguments = Bundle().apply {
                    putInt("char_id", char_id)
                }
            }
    }
}