package com.code.tvshowexplorer.persistance

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore by preferencesDataStore("user_session")

object SessionManager {
    private const val PREF_NAME = "session_preferences"
    private const val SESSION_ID_KEY = "session_id"

    // Save the session ID/token when the user logs in
    fun saveSessionId(context: Context, sessionId: String) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(SESSION_ID_KEY, sessionId).apply()
    }

    // Retrieve the session ID/token if available
    fun getSessionId(context: Context): String? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(SESSION_ID_KEY, null)
    }

    // Clear the session (on logout)
    fun clearSession(context: Context) {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(SESSION_ID_KEY).apply()
    }

    fun getUserEmail(context: Context): String? {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(SESSION_ID_KEY, null)
    }

}

