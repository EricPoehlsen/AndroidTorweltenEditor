package de.aequinoktium.twedit

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    lateinit var dbc: DatabaseConnect
    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // first run?
        var prefs = this.getSharedPreferences("base", 0)

        var fm = this.supportFragmentManager

        var installed = prefs.getBoolean("installed", false)
        if (!installed) {

        }

        dbc = DatabaseConnect(this.applicationContext)
        db = dbc.writableDatabase


    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }
}