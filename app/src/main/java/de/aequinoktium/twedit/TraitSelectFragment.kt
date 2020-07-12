package de.aequinoktium.twedit

import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.marginBottom
import androidx.fragment.app.activityViewModels

/**
 * A simple [Fragment] subclass.
 * Use the [TraitSelectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TraitSelectFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_trait_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity

        val traits: Cursor = act.db.rawQuery(
            "SELECT id, name, xp_cost FROM traits",
            null
        )

        val container = act.findViewById<LinearLayout>(R.id.traitselect_container)
        while (traits.moveToNext()) {
            var tv = TraitView(context)
            var lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.setMargins(0, 0, act.calc_dp(4), 0)

            tv.layoutParams = lp
            tv.trait_id = traits.getInt(0)
            tv.title.text = traits.getString(1)
            tv.xp.text = traits.getInt(2).toString()

            container.addView(tv)




        }



    }
}