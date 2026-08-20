package com.example.codise

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ServicioApi
import com.example.codise.data.Evento
import com.example.codise.data.SolicitudEvento
import com.example.codise.data.AdministradorSesion
import com.example.codise.utils.UtilidadesRed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class EstadoUiEventos {
    object Inactivo : EstadoUiEventos()
    object Cargando : EstadoUiEventos()
    data class Exito(val eventos: List<Evento>) : EstadoUiEventos()
    data class Error(val mensaje: String) : EstadoUiEventos()
}

class ViewModelEventos(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)
    private val utilidadesRed = UtilidadesRed(aplicacion)

    private val _estadoUi = mutableStateOf<EstadoUiEventos>(EstadoUiEventos.Inactivo)
    val estadoUi: State<EstadoUiEventos> = _estadoUi

    private val _estaSubiendo = mutableStateOf(false)
    val estaSubiendo: State<Boolean> = _estaSubiendo

    private val _subidaExitosa = mutableStateOf(false)
    val subidaExitosa: State<Boolean> = _subidaExitosa

    init {
        obtenerEventos()
        observarConectividad()
    }

    private fun observarConectividad() {
        viewModelScope.launch {
            utilidadesRed.estaConectado.collectLatest { estaConectado ->
                if (estaConectado) {
                    sincronizarAsistenciaPendiente()
                }
            }
        }
    }

    private fun sincronizarAsistenciaPendiente() {
        val pendientes = administradorSesion.obtenerAsistenciasPendientes()
        if (pendientes.isNotEmpty()) {
            pendientes.forEach { idEvento ->
                registrarAsistencia(idEvento)
            }
        }
    }

    fun obtenerEventos() {
        viewModelScope.launch {
            _estadoUi.value = EstadoUiEventos.Cargando
            try {
                val respuesta = servicioApi.obtenerEventos()
                if (respuesta.isSuccessful) {
                    _estadoUi.value = EstadoUiEventos.Exito(respuesta.body() ?: emptyList())
                } else {
                    _estadoUi.value = EstadoUiEventos.Error("Error: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                _estadoUi.value = EstadoUiEventos.Error("Error de red: ${e.message}")
            }
        }
    }

    fun subirEvento(solicitudEvento: SolicitudEvento) {
        val sesion = administradorSesion.obtenerSesion() ?: return
        val token = "Bearer ${sesion.tokens.access}"

        viewModelScope.launch {
            _estaSubiendo.value = true
            try {
                val respuesta = servicioApi.crearEvento(token, solicitudEvento)
                if (respuesta.isSuccessful) {
                    _subidaExitosa.value = true
                    obtenerEventos()
                }
            } catch (e: Exception) {
                // Manejo de error
            } finally {
                _estaSubiendo.value = false
            }
        }
    }

    fun reiniciarEstadoSubida() {
        _subidaExitosa.value = false
    }

    fun registrarAsistencia(idEvento: Int) {
        val sesion = administradorSesion.obtenerSesion() ?: return
        val token = "Bearer ${sesion.tokens.access}"

        if (!utilidadesRed.tieneInternet()) {
            administradorSesion.agregarAsistenciaPendiente(idEvento)
            return
        }

        viewModelScope.launch {
            try {
                val respuesta = servicioApi.registrarAsistencia(token, idEvento)
                if (respuesta.isSuccessful) {
                    administradorSesion.eliminarAsistenciaPendiente(idEvento)
                } else {
                    administradorSesion.agregarAsistenciaPendiente(idEvento)
                }
            } catch (e: Exception) {
                administradorSesion.agregarAsistenciaPendiente(idEvento)
            }
        }
    }
}
