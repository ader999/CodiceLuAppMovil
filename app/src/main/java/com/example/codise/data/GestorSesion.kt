package com.example.codise.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AdministradorSesion private constructor(contexto: Context) {
    private val preferencias: SharedPreferences = contexto.getSharedPreferences(NOMBRE_PREF, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _sesion = MutableStateFlow<RespuestaAutenticacion?>(null)
    val sesion: StateFlow<RespuestaAutenticacion?> = _sesion

    init {
        _sesion.value = obtenerSesionDePreferencias()
    }

    companion object {
        private const val NOMBRE_PREF = "codise_session"
        private const val CLAVE_RESPUESTA_AUTH = "auth_response"
        private const val CLAVE_ASISTENCIA_PENDIENTE = "pending_attendance"

        @Volatile
        private var instancia: AdministradorSesion? = null

        fun obtenerInstancia(contexto: Context): AdministradorSesion {
            return instancia ?: synchronized(this) {
                instancia ?: AdministradorSesion(contexto.applicationContext).also { instancia = it }
            }
        }
    }

    fun guardarSesion(respuestaAutenticacion: RespuestaAutenticacion) {
        val json = gson.toJson(respuestaAutenticacion)
        preferencias.edit().putString(CLAVE_RESPUESTA_AUTH, json).apply()
        _sesion.value = respuestaAutenticacion
    }

    private fun obtenerSesionDePreferencias(): RespuestaAutenticacion? {
        val json = preferencias.getString(CLAVE_RESPUESTA_AUTH, null)
        return if (json != null) {
            gson.fromJson(json, RespuestaAutenticacion::class.java)
        } else {
            null
        }
    }

    fun obtenerSesion(): RespuestaAutenticacion? = _sesion.value

    fun cerrarSesion() {
        preferencias.edit().remove(CLAVE_RESPUESTA_AUTH).apply()
        _sesion.value = null
    }

    fun agregarAsistenciaPendiente(idEvento: Int) {
        val actual = obtenerAsistenciasPendientes().toMutableSet()
        actual.add(idEvento)
        guardarAsistenciasPendientes(actual)
    }

    fun obtenerAsistenciasPendientes(): Set<Int> {
        val json = preferencias.getString(CLAVE_ASISTENCIA_PENDIENTE, null)
        return if (json != null) {
            gson.fromJson(json, Array<Int>::class.java).toSet()
        } else {
            emptySet()
        }
    }

    fun eliminarAsistenciaPendiente(idEvento: Int) {
        val actual = obtenerAsistenciasPendientes().toMutableSet()
        actual.remove(idEvento)
        guardarAsistenciasPendientes(actual)
    }

    private fun guardarAsistenciasPendientes(idsEventos: Set<Int>) {
        val json = gson.toJson(idsEventos.toTypedArray())
        preferencias.edit().putString(CLAVE_ASISTENCIA_PENDIENTE, json).apply()
    }
}
