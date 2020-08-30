package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * A ViewModel to handle data that is not directly character related.
 */

class DataViewModel: ViewModel() {
    private lateinit var db: SQLiteDatabase

    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
    }

    /**
     * Load available traits from the SQLite database
     * @return an Array of [TraitData]
     */
    suspend fun loadTraits(): Array<TraitData> {
        var traits = arrayOf<TraitData>()
        var sql = """
            SELECT
                id,
                name, 
                cls, 
                grp,
                txt,
                min_rank, 
                max_rank,
                xp_cost,
                effects
            FROM 
                traits
            ORDER BY
            cls, grp
        """.trimIndent()
        var data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            var trait = TraitData()
            trait.id = data.getInt(0)
            trait.name = data.getString(1)
            trait.cls = data.getInt(2)
            trait.grp = data.getInt(3)
            trait.txt = data.getString(4)
            trait.min_rank = data.getInt(5)
            trait.cur_rank = trait.min_rank
            trait.max_rank = data.getInt(6)
            trait.xp = data.getInt(7)
            trait.effects = data.getString(8)

            sql = """
                SELECT 
                    id,
                    name,
                    xp_factor,
                    oper, 
                    grp,
                    txt
                FROM 
                    trait_vars
                WHERE trait_id = ${trait.id}
            """.trimIndent()
            val variants: MutableMap<String, MutableMap<Int, TraitVariant>> = mutableMapOf()
            val trait_vars = db.rawQuery(sql, null)
            while (trait_vars.moveToNext()) {
                val variant = TraitVariant()
                variant.var_id = trait_vars.getInt(0)
                variant.name = trait_vars.getString(1)
                variant.xp_factor = trait_vars.getFloat(2)
                variant.oper = trait_vars.getInt(3)
                variant.grp = trait_vars.getString(4)
                variant.txt = trait_vars.getString(5)
                if (variants[variant.grp] == null) {
                    variants[variant.grp] = mutableMapOf(Pair(variant.var_id, variant))
                } else {
                    variants[variant.grp]?.set(variant.var_id, variant)
                }
            }
            trait_vars.close()
            trait.variants = variants
            traits += trait
        }
        data.close()
        return traits
    }

    /**
     * Retrieves some character info from the database
     * @param name partial search string used to formulate the WHERE clause
     * @return an array of CharInfo
     */
    suspend fun findCharacters(name: String = ""): Array<CharInfo> {
        var result = emptyArray<CharInfo>()

        var sql = """
            SELECT 
                id, 
                name, 
                xp_used, 
                xp_total,
                deleted
            FROM 
                char_core 
        """.trimIndent()

        if (name.length > 0) {
            val select = name.replace("'", "\u2019")
            sql += " WHERE name LIKE '%$select%'"
        }

        sql += " ORDER BY name"

        val data: Cursor = db.rawQuery(sql, null)

        while (data.moveToNext()) {
            val char_info = CharInfo()
            char_info.id = data.getInt(0)
            char_info.name = data.getString(1)
            char_info.xp_free = data.getInt(3) - data.getInt(2)
            char_info.xp_total = data.getInt(3)
            char_info.deleted = (data.getInt(4) == 1)
            sql = """
                SELECT 
                    txt 
                FROM 
                    char_info
                WHERE
                    char_id = ${char_info.id}
                    AND
                    name = 'concept'
            """.trimIndent()
            val concept = db.rawQuery(sql, null)
            if (concept.moveToFirst()) {
                char_info.concept = concept.getString(0).toString()
            }
            concept.close()
            result += char_info
        }

        data.close()
        return result
    }

    suspend fun addCharacter(name: String) {
        val checked_name = name.replace("'", "\u2019")
        var data = ContentValues()
        data.put("name", name)
        data.put("xp_total", 330)
        db.insert("char_core", null, data)
    }

    suspend fun deleteCharacter(char: CharInfo) {
        if (char.deleted) { // wipe character
            // find character accounts
            var sql = """
                SELECT id
                FROM accounts
                WHERE char_id = ${char.id}
            """.trimIndent()
            var data = db.rawQuery(sql, null)
            var accounts = ""
            while (data.moveToNext()) {
                accounts += data.getInt(0).toString() + ","
            }
            data.close()

            // remove money transfers to and from 'bank'
            if (!accounts.isBlank()) {
                accounts = accounts.dropLast(1)
                sql = """
                    DELETE FROM money_transfers
                    WHERE 
                        (origin_acc IN ($accounts) AND target_acc = 0) 
                        OR
                        (origin_acc = 0 and target_acc IN ($accounts))
                    """.trimIndent()
                db.execSQL(sql)
            }

            // delete character information from other tables
            val tables = arrayOf(
                "accounts",
                "char_items",
                "char_traits",
                "char_skills",
                "char_info"
            )
            for (table in tables) {
                sql = """
                    DELETE FROM $table
                    WHERE char_id = ${char.id}
                """.trimIndent()
                db.execSQL(sql)
            }

            // remove core information
            sql = """ 
                DELETE FROM char_core
                WHERE id = ${char.id}    
            """.trimMargin()
            db.execSQL(sql)
        } else { // mark as deleted
            val sql = """
                UPDATE char_core
                SET 
                    deleted = 1,
                    name = '#${char.name}'
                WHERE id = ${char.id}
            """.trimIndent()
            db.execSQL(sql)
        }
    }



    /**
     * View Model is destroyed. Clean up the database connection.
     */
    override fun onCleared() {
        db.close()
        super.onCleared()
    }

    class CharInfo {
        var id = 0
        var name = ""
        var concept = ""
        var xp_free = 0
        var xp_total = 0
        var deleted = false
    }
}