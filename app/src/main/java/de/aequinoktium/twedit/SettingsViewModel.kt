package de.aequinoktium.twedit

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.ViewModel

/**
 * A ViewModel to handle user settings for the app
 */

class SettingsViewModel: ViewModel() {
    private lateinit var db: SQLiteDatabase
    private val settings = mutableMapOf<String, String>()


    fun setDatabase(db: SQLiteDatabase) {
        this.db = db
    }


}