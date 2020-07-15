package de.aequinoktium.twedit

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {
    lateinit var dbc: DatabaseConnect
    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        dbc = DatabaseConnect(this.applicationContext)
        db = dbc.writableDatabase

        val c: CharacterViewModel by viewModels()
        c.setDatabase(db)
        // first run?
        var prefs = this.getSharedPreferences("base", 0)

        var installed = prefs.getBoolean("installed", false)
        if (!installed) {

        }
    }




    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    /**
     * Helper function to calculate the px from dp
     * @param dp an integer value of dp pixels
     * @return the appropriate amount of physical pixels
     */
    fun calc_dp(dp: Int): Int {
        val scale: Float = resources.displayMetrics.density
        var px: Int = (dp * scale + 0.5f).toInt()
        return px
    }


}