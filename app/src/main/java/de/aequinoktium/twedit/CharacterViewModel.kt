package de.aequinoktium.twedit

import android.database.sqlite.SQLiteDatabase
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
    var attribs: Map<String, Int> = mapOf(
        "phy" to 0,
        "men" to 0,
        "soz" to 0,
        "nk" to 0,
        "fk" to 0,
        "lp" to 0,
        "ep" to 0,
        "mp" to 0
    )


    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
    }

    fun loadCharData(char_id: Int) {
        var sql: String = ""

        sql = "SELECT * FROM char_core WHERE id = $char_id"


    }



    /**
     * View Model is destroyed. Clean up the database connection.
     */
    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}