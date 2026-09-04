package com.example.codise.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

enum class IdiomaApp(
    val codigo: String,
    val etiqueta: String,
    val etiquetaNativa: String,
    val cabeceraHttp: String,
    val bandera: String
) {
    ESPANOL("es", "Español", "Español", "es", "🇳🇮"),
    INGLES("en", "Inglés", "English", "en", "🇺🇸"),
    CHINO("zh", "Chino Mandarín", "中文 (简体)", "zh-CN", "🇨🇳");

    companion object {
        fun desdeCodigo(codigo: String?): IdiomaApp {
            if (codigo == null) return ESPANOL
            val limpio = codigo.lowercase().trim()
            return when {
                limpio.startsWith("zh") -> CHINO
                limpio.startsWith("en") -> INGLES
                else -> ESPANOL
            }
        }
    }
}

class GestorIdioma private constructor(contexto: Context) {
    private val preferencias: SharedPreferences =
        contexto.getSharedPreferences(NOMBRE_PREF, Context.MODE_PRIVATE)

    private val _idiomaActual = MutableStateFlow(obtenerIdiomaInicial())
    val idiomaActual: StateFlow<IdiomaApp> = _idiomaActual

    private fun obtenerIdiomaInicial(): IdiomaApp {
        val codigoGuardado = preferencias.getString(CLAVE_IDIOMA, null)
        if (codigoGuardado != null) {
            return IdiomaApp.desdeCodigo(codigoGuardado)
        }
        val idiomaSistema = Locale.getDefault().language
        return IdiomaApp.desdeCodigo(idiomaSistema)
    }

    fun cambiarIdioma(nuevoIdioma: IdiomaApp) {
        preferencias.edit().putString(CLAVE_IDIOMA, nuevoIdioma.codigo).apply()
        _idiomaActual.value = nuevoIdioma
    }

    fun obtenerCabeceraAcceptLanguage(): String {
        return _idiomaActual.value.cabeceraHttp
    }

    companion object {
        private const val NOMBRE_PREF = "codise_idioma_prefs"
        private const val CLAVE_IDIOMA = "idioma_seleccionado"

        @Volatile
        private var instancia: GestorIdioma? = null

        fun obtenerInstancia(contexto: Context): GestorIdioma {
            return instancia ?: synchronized(this) {
                instancia ?: GestorIdioma(contexto.applicationContext).also { instancia = it }
            }
        }
    }
}
