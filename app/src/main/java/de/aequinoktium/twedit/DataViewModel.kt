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
     * View Model is destroyed. Clean up the database connection.
     */
    override fun onCleared() {
        db.close()
        super.onCleared()
    }
}