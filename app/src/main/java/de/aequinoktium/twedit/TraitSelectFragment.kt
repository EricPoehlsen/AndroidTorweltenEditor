package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A simple [Fragment] subclass.
 * Use the [TraitSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TraitSelectFragment : Fragment() {
    var char_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            char_id = it.getInt("char_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trait_select, container, false)
    }

    companion object {
        /**
         * Factory method to create the fragment
         *
         * @param char_id character_id
         * @return A new instance of fragment TraitSelectFragment.
         */
        @JvmStatic
        fun newInstance(char_id: Int) =
            TraitSelectFragment().apply {
                arguments = Bundle().apply {
                    putInt("char_id", char_id)
                }
            }
    }
}