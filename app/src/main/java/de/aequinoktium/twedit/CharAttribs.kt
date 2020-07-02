package de.aequinoktium.twedit

import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CharAttribs.newInstance] factory method to
 * create an instance of this fragment.
 */
class CharAttribs(char_id: Int) : Fragment() {
    // TODO: Rename and change types of parameters
    var char_id = char_id

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_char_attribs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var act = activity as MainActivity
        var data: Cursor = act.db.rawQuery("SELECT * FROM char_core WHERE id = $char_id", null)
        var x = data.count
        Log.d("info", "Selected $x entities ...")




    }


}