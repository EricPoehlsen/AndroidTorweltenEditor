package de.aequinoktium.twedit

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Primary ViewModel for anything related to the character
 * handles all database and network IO.
 */

class CharacterViewModel: ViewModel() {
    lateinit var db: SQLiteDatabase
    var char_id: Int = 0
    var name: String = ""
    var attribs = mutableMapOf(
        "phy" to 0,
        "men" to 0,
        "soz" to 0,
        "nk" to 0,
        "fk" to 0,
        "lp" to 0,
        "ep" to 0,
        "mp" to 0
    )
    var xp_used: Int = 0
    var xp_total: Int = 0


    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
    }

    fun loadCharData(char_id: Int) {
        this.char_id = char_id
        var sql: String = ""
        sql = "SELECT * FROM char_core WHERE id = $char_id"
        var data: Cursor = db.rawQuery(sql, null)
        if (data.count == 1) {
            data.moveToFirst()
            var attrib_names = arrayOf("phy", "men", "soz", "nk", "fk", "lp", "ep", "mp")
            for (n in attrib_names) {
                attribs[n] = data.getInt(data.getColumnIndex(n))
            }
            xp_used = data.getInt(data.getColumnIndex("xp_used"))
            xp_total = data.getInt(data.getColumnIndex("xp_total"))
            name = data.getString(data.getColumnIndex("name"))
        }
   }



    /**
     * View Model is destroyed. Clean up the database connection.
     */
    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}