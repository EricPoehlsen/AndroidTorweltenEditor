package de.aequinoktium.twedit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Making sure that the
 */
class SetupFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var root: View = inflater.inflate(
            R.layout.fragment_setup,
            container,
            false)
        return root
    }

    /*
    fun createDatabase() {
        this.activity?.findViewById<LinearLayout>(R.id.setup_layout)
        val scope = CoroutineScope(Dispatchers.Main)
        var c = this.activity?.findViewById<LinearLayout>(R.id.setup_layout)


        scope.launch {
            delay(10000)
            Log.d("info", "waited ...")
            var t = TextView(context)
            t.setText("Here it happened")
            c?.addView(t)
        }

    }
    */
}