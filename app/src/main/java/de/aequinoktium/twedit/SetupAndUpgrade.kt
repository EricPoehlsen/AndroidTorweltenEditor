package de.aequinoktium.twedit

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.*

/**
 * Making sure that the
 */
class SetupAndUpgrade : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var root: View = inflater.inflate(
            R.layout.fragment_setup_and_upgrade,
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