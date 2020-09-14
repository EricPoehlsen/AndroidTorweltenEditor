package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.launch

/**
 * A ViewModel to handle user settings for the app
 */

class SettingsViewModel: ViewModel() {
    private lateinit var db: SQLiteDatabase
    private val settings = mutableMapOf<String, String>()

    /**
     * initialize the database for the ViewModel
     */
    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
        this.viewModelScope.launch(Dispatchers.IO) {
            loadSettings()
        }
    }

    fun loadSettings() {
        val sql = "SELECT * FROM settings"
        val data = db.rawQuery(sql, null)
        while (data.moveToNext()) {
            val key = data.getString(data.getColumnIndex("name"))
            val value = data.getString(data.getColumnIndex("value"))
            settings[key] = value
        }

        data.close()
    }

    /**
     * update or set a key value pair with a boolean value
     * @param key the setting name - a String
     * @param value the setting value - a Boolean
     * @return the value
     */
    fun update(key: String, value: Boolean): Boolean {
        val entry = if (value) "1" else "0"
        this.viewModelScope.launch(Dispatchers.IO) {
            update_setting(key,  entry)
        }
        return value
    }

    /**
     * update or set a key value pair with an integer value
     * @param key the setting name - a String
     * @param value the setting value - an Integer
     * @return the value
     */
    fun update(key: String, value: Int): Int {
        val entry = value.toString()
        this.viewModelScope.launch(Dispatchers.IO) {
            update_setting(key,  entry)
        }
        return value
    }

    /**
     * retrieve the value of a key from [settings]
     * @param key - name of the setting as String
     * @return value of the setting as String "0" if not found
     */
    fun getString(key: String): String {
        var result = "0"
        if (settings[key] != null) {
            result = settings[key]!!
        }
        return result
    }

    fun getInt(key: String) = getString(key).toInt()
    fun getBoolean(key: String) = getString(key) == "1"


    /**
     * write a key, value pair to the database and update [settings]
     * @param key - setting key as String
     * @param value - setting value as String
     */
    private suspend fun update_setting(key: String, value: String) {
        if (key in settings.keys) {
            val sql = "UPDATE settings SET value = '$value' WHERE name = '$key'"
            db.execSQL(sql)
        } else {
            val sql = "INSERT INTO settings (name, value) VALUES ('$key', '$value')"
            db.execSQL(sql)
        }
        settings[key] = value
    }





}