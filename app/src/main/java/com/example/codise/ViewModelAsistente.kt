package com.example.codise

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.*
import kotlinx.coroutines.launch
import java.util.UUID

data class MensajeChat(
    val id: String = UUID.randomUUID().toString(),
    val emisor: EmisorMensaje,
    val texto: String,
    val fecha: Long = System.currentTimeMillis(),
    val herramientas: List<HerramientaUtilizada>? = null,
    val modelo: String? = null,
    val esError: Boolean = false
)

enum class EmisorMensaje {
    USUARIO,
    ASISTENTE
}

class ViewModelAsistente(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)

    private val _mensajes = mutableStateOf<List<MensajeChat>>(emptyList())
    val mensajes: State<List<MensajeChat>> = _mensajes

    private val _estaEscribiendo = mutableStateOf(false)
    val estaEscribiendo: State<Boolean> = _estaEscribiendo

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _ubicacionGps = mutableStateOf<UbicacionGps?>(null)
    val ubicacionGps: State<UbicacionGps?> = _ubicacionGps

    private var ultimoIdiomaCodigo: String = "es"

    fun actualizarUbicacion(lat: Double?, lng: Double?) {
        if (lat != null && lng != null) {
            _ubicacionGps.value = UbicacionGps(lat, lng)
        }
    }

    fun limpiarUbicacion() {
        _ubicacionGps.value = null
    }

    fun inicializarBienvenida(mensajeBienvenida: String, idiomaCodigo: String) {
        if (_mensajes.value.isEmpty()) {
            _mensajes.value = listOf(
                MensajeChat(
                    emisor = EmisorMensaje.ASISTENTE,
                    texto = mensajeBienvenida
                )
            )
            ultimoIdiomaCodigo = idiomaCodigo
        } else if (_mensajes.value.size == 1 && _mensajes.value[0].emisor == EmisorMensaje.ASISTENTE && ultimoIdiomaCodigo != idiomaCodigo) {
            // Si el usuario cambió de idioma y solo estaba el saludo inicial, actualizarlo
            _mensajes.value = listOf(
                MensajeChat(
                    id = _mensajes.value[0].id,
                    emisor = EmisorMensaje.ASISTENTE,
                    texto = mensajeBienvenida
                )
            )
            ultimoIdiomaCodigo = idiomaCodigo
        }
    }

    fun enviarMensaje(
        textoMensaje: String,
        idiomaCodigo: String,
        latitudGps: Double? = null,
        longitudGps: Double? = null,
        mensajeErrorPorDefecto: String = "No se pudo conectar con Eduardo"
    ) {
        val textoLimpio = textoMensaje.trim()
        if (textoLimpio.isBlank() || _estaEscribiendo.value) return

        ultimoIdiomaCodigo = idiomaCodigo

        if (latitudGps != null && longitudGps != null) {
            _ubicacionGps.value = UbicacionGps(latitudGps, longitudGps)
        }

        val mensajeUsuario = MensajeChat(
            emisor = EmisorMensaje.USUARIO,
            texto = textoLimpio
        )

        // Limpiar posible mensaje de error anterior si lo hubo
        val listaFiltrada = _mensajes.value.filter { !it.esError }
        _mensajes.value = listaFiltrada + mensajeUsuario
        _estaEscribiendo.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val sesion = administradorSesion.obtenerSesion()
                val token = sesion?.let { "Bearer ${it.tokens.access}" }

                // Historial de conversación previo (excluyendo el saludo inicial y mensajes de error)
                val historialItems = _mensajes.value
                    .dropLast(1)
                    .drop(if (_mensajes.value.firstOrNull()?.emisor == EmisorMensaje.ASISTENTE) 1 else 0)
                    .filter { !it.esError && it.texto.isNotBlank() }
                    .map {
                        ChatHistoryItem(
                            role = if (it.emisor == EmisorMensaje.USUARIO) "user" else "model",
                            parts = listOf(it.texto)
                        )
                    }

                val coords = _ubicacionGps.value ?: if (latitudGps != null && longitudGps != null) UbicacionGps(latitudGps, longitudGps) else null

                val solicitud = SolicitudAsistente(
                    mensaje = textoLimpio,
                    idioma = idiomaCodigo,
                    ubicacion = coords,
                    historial = if (historialItems.isNotEmpty()) historialItems else null
                )

                val respuesta = servicioApi.enviarMensajeAsistente(
                    token = token,
                    solicitud = solicitud
                )

                if (respuesta.isSuccessful && respuesta.body() != null) {
                    val cuerpo = respuesta.body()!!
                    val mensajeAsistente = MensajeChat(
                        emisor = EmisorMensaje.ASISTENTE,
                        texto = cuerpo.respuesta,
                        herramientas = cuerpo.herramientas_utilizadas,
                        modelo = cuerpo.modelo_utilizado
                    )
                    _mensajes.value = _mensajes.value + mensajeAsistente
                } else {
                    val errorMensaje = respuesta.errorBody()?.string()
                    val mensajeError = MensajeChat(
                        emisor = EmisorMensaje.ASISTENTE,
                        texto = mensajeErrorPorDefecto,
                        esError = true
                    )
                    _mensajes.value = _mensajes.value + mensajeError
                    _error.value = "Error ${respuesta.code()}: $errorMensaje"
                }
            } catch (e: Exception) {
                val mensajeError = MensajeChat(
                    emisor = EmisorMensaje.ASISTENTE,
                    texto = mensajeErrorPorDefecto,
                    esError = true
                )
                _mensajes.value = _mensajes.value + mensajeError
                _error.value = e.localizedMessage
            } finally {
                _estaEscribiendo.value = false
            }
        }
    }

    fun reiniciarConversacion(mensajeBienvenida: String, idiomaCodigo: String) {
        ultimoIdiomaCodigo = idiomaCodigo
        _mensajes.value = listOf(
            MensajeChat(
                emisor = EmisorMensaje.ASISTENTE,
                texto = mensajeBienvenida
            )
        )
        _error.value = null
        _estaEscribiendo.value = false
    }
}
