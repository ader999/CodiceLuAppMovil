package com.example.codise.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _session = MutableStateFlow<AuthResponse?>(null)
    val session: StateFlow<AuthResponse?> = _session

    init {
        _session.value = getSessionFromPrefs()
    }

    companion object {
        private const val PREF_NAME = "codise_session"
        private const val KEY_AUTH_RESPONSE = "auth_response"
        private const val KEY_PENDING_ATTENDANCE = "pending_attendance"

        @Volatile
        private var instance: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return instance ?: synchronized(this) {
                instance ?: SessionManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun saveSession(authResponse: AuthResponse) {
        val json = gson.toJson(authResponse)
        prefs.edit().putString(KEY_AUTH_RESPONSE, json).apply()
        _session.value = authResponse
    }

    private fun getSessionFromPrefs(): AuthResponse? {
        val json = prefs.getString(KEY_AUTH_RESPONSE, null)
        return if (json != null) {
            gson.fromJson(json, AuthResponse::class.java)
        } else {
            null
        }
    }

    fun getSession(): AuthResponse? = _session.value

    fun clearSession() {
        prefs.edit().remove(KEY_AUTH_RESPONSE).apply()
        _session.value = null
    }

    fun addPendingAttendance(eventId: Int) {
        val current = getPendingAttendance().toMutableSet()
        current.add(eventId)
        savePendingAttendance(current)
    }

    fun getPendingAttendance(): Set<Int> {
        val json = prefs.getString(KEY_PENDING_ATTENDANCE, null)
        return if (json != null) {
            gson.fromJson(json, Array<Int>::class.java).toSet()
        } else {
            emptySet()
        }
    }

    fun removePendingAttendance(eventId: Int) {
        val current = getPendingAttendance().toMutableSet()
        current.remove(eventId)
        savePendingAttendance(current)
    }

    private fun savePendingAttendance(eventIds: Set<Int>) {
        val json = gson.toJson(eventIds.toTypedArray())
        prefs.edit().putString(KEY_PENDING_ATTENDANCE, json).apply()
    }
}
