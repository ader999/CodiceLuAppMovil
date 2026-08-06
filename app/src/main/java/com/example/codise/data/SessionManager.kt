package com.example.codise.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "codise_session"
        private const val KEY_AUTH_RESPONSE = "auth_response"
    }

    fun saveSession(authResponse: AuthResponse) {
        val json = gson.toJson(authResponse)
        prefs.edit().putString(KEY_AUTH_RESPONSE, json).apply()
    }

    fun getSession(): AuthResponse? {
        val json = prefs.getString(KEY_AUTH_RESPONSE, null)
        return if (json != null) {
            gson.fromJson(json, AuthResponse::class.java)
        } else {
            null
        }
    }

    fun clearSession() {
        prefs.edit().remove(KEY_AUTH_RESPONSE).apply()
    }
}
