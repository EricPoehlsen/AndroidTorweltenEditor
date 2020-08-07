package de.aequinoktium.twedit

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.*

/**
 * Primary ViewModel for anything related to the character
 * handles some database and network IO.
 */

class CharacterViewModel: ViewModel() {
    val LOCALE = Locale.GERMAN
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
    var info = mutableMapOf(
        "core" to mutableListOf<Info>(),
        "desc" to mutableListOf<Info>()
    )

    var inv = mutableListOf<Item>()


    var xp_used: Int = 0
    var xp_total: Int = 0

    var edit_trait = 0
    var char_traits = emptyArray<Int>()

    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
    }

    suspend fun loadCharData(char_id: Int) {
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
        data.close()

        sql = "SELECT trait_id FROM char_traits WHERE char_id = $char_id"
        data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            char_traits += data.getInt(0)
        }

        data.close()

        loadInfo()
    }

    /**
     * Load character info from the database
     * Initialize the core fields on first load
     */
    suspend fun loadInfo() {
        var reload = false
        var sql = """
            SELECT
                id,
                name, 
                txt, 
                dataset
            FROM
                char_info
            WHERE
                char_id = $char_id 
        """.trimIndent()
        var data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            val dataset = data.getString(3)
            var i = Info()
            i.info_id = data.getInt(0)
            i.name = data.getString(1)
            i.txt = data.getString(2)
            if (info[dataset] != null) {
                info[dataset]?.add(i)
            } else {
                info[dataset] = mutableListOf(i)
            }
        }
        data.close()

        if (info["core"]?.size == 0) {
            sql = """
                INSERT INTO
                    char_info
                    (char_id, name, dataset)
                VALUES
                    ($char_id, 'species', 'core'),
                    ($char_id, 'concept', 'core'),
                    ($char_id, 'homeworld', 'core'),
                    ($char_id, 'culture', 'core'),
                    ($char_id, 'notes', 'core')
            """.trimIndent()
            db.execSQL(sql)
            reload = true
        }

        if (info["desc"]?.size == 0) {
            sql = """
                INSERT INTO
                    char_info
                    (char_id, name, dataset)
                VALUES
                    ($char_id, 'age', 'desc'),
                    ($char_id, 'size', 'desc'),
                    ($char_id, 'weight', 'desc'),
                    ($char_id, 'sex', 'desc'),
                    ($char_id, 'build', 'desc'),
                    ($char_id, 'eyecolor', 'desc'),
                    ($char_id, 'color1', 'desc'),
                    ($char_id, 'color2', 'desc'),
                    ($char_id, 'desc', 'desc')
            """.trimIndent()
            db.execSQL(sql)
            reload = true
        }

        if (reload) {
            loadInfo()
        }


    }



    /**
     * View Model is destroyed. Clean up the database connection.
     */
    override fun onCleared() {
        db.close()
        super.onCleared()
    }

    class Info() {
        var info_id = 0
        var name = ""
        var txt = ""
    }
}