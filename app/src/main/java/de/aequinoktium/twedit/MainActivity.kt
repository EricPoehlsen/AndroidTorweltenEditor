package de.aequinoktium.twedit

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {
    lateinit var db: SQLiteDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val dbc = DatabaseConnect(this.applicationContext)
        db = dbc.writableDatabase

        val c: CharacterViewModel by viewModels()
        c.setDatabase(db)
        val d: DataViewModel by viewModels()
        d.setDatabase(db)
        val settings: SettingsViewModel by viewModels()
        settings.setDatabase(db)



        setStringValues(c)

        // first run?
        val prefs = this.getSharedPreferences("base", 0)

        val installed = prefs.getBoolean("installed", false)
        if (!installed) {

        }
    }

    fun setStringValues(c: CharacterViewModel) {
        c.string_values["mat"] = getString(R.string.cinv_material)
    }




    override fun onDestroy() {
        super.onDestroy()
        db.close()
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