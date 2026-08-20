package com.example.codise

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ServicioApi
import com.example.codise.data.Ciudad
import com.example.codise.data.Circuito
import com.example.codise.data.AdministradorSesion
import com.example.codise.data.SolicitudVisita
import com.example.codise.data.local.BaseDeDatosApp
import com.example.codise.data.local.PuntoVisitado
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ViewModelPrincipal(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)
    private val baseDeDatos = BaseDeDatosApp.obtenerBaseDeDatos(aplicacion)
    private val puntoVisitadoDao = baseDeDatos.puntoVisitadoDao()

    val idsPuntosVisitados = puntoVisitadoDao.obtenerTodosLosVisitados()
        .map { lista -> lista.map { it.puntoInteresId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val puntosVisitados = puntoVisitadoDao.obtenerTodosLosVisitados()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ciudades = mutableStateOf<List<Ciudad>>(emptyList())
    val ciudades: State<List<Ciudad>> = _ciudades

    private val _estaCargando = mutableStateOf(false)
    val estaCargando: State<Boolean> = _estaCargando

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _idCiudadSeleccionada = mutableStateOf<Int?>(null)
    val ciudadSeleccionada: Ciudad?
        get() = ciudades.value.find { it.id == _idCiudadSeleccionada.value }

    private val _idCircuitoSeleccionado = mutableStateOf<Int?>(null)
    val idCircuitoSeleccionado: Int? get() = _idCircuitoSeleccionado.value

    val circuitoSeleccionado: Circuito?
        get() = ciudadSeleccionada?.circuitos?.find { it.id == _idCircuitoSeleccionado.value }

    private var ultimaHoraObtencion = 0L
    private val TIEMPO_ESPERA_MS = 5 * 60 * 1000 // 5 minutos

    init {
        obtenerCiudades()
        sincronizarPuntosVisitados()
    }

    fun sincronizarPuntosVisitados() {
        val sesion = administradorSesion.obtenerSesion() ?: return
        val token = "Bearer ${sesion.tokens.access}"

        viewModelScope.launch {
            try {
                val respuesta = servicioApi.obtenerVisitas(token)
                if (respuesta.isSuccessful) {
                    val visitasNube = respuesta.body() ?: emptyList()
                    puntoVisitadoDao.eliminarTodos()
                    puntoVisitadoDao.insertarTodos(visitasNube.map { 
                        PuntoVisitado(it.puntoInteresId, estaValidado = it.esValidada) 
                    })
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seleccionarCiudad(id: Int?) {
        _idCiudadSeleccionada.value = id
        _idCircuitoSeleccionado.value = null
    }

    fun seleccionarCircuito(id: Int?) {
        _idCircuitoSeleccionado.value = id
    }

    fun alternarVisitado(puntoInteresId: Int, latitud: Double? = null, longitud: Double? = null) {
        viewModelScope.launch {
            val estaActualmenteVisitado = idsPuntosVisitados.value.contains(puntoInteresId)
            
            if (estaActualmenteVisitado) {
                puntoVisitadoDao.desmarcarComoVisitado(PuntoVisitado(puntoInteresId))
            } else {
                puntoVisitadoDao.marcarComoVisitado(PuntoVisitado(puntoInteresId))
            }

            val sesion = administradorSesion.obtenerSesion()
            if (sesion != null && !estaActualmenteVisitado) {
                val token = "Bearer ${sesion.tokens.access}"
                try {
                    val solicitudVisita = SolicitudVisita(puntoInteresId, latitud, longitud)
                    val respuesta = servicioApi.registrarVisita(token, solicitudVisita)
                    if (respuesta.isSuccessful) {
                        val respuestaVisita = respuesta.body()
                        if (respuestaVisita != null && respuestaVisita.esValidada) {
                            puntoVisitadoDao.marcarComoVisitado(PuntoVisitado(puntoInteresId, estaValidado = true))
                        }
                    } else {
                        _error.value = "Error al sincronizar con la nube: ${respuesta.code()}"
                    }
                } catch (e: Exception) {
                    _error.value = "Error de red al sincronizar: ${e.message}"
                }
            }
        }
    }

    fun obtenerCiudades(forzar: Boolean = false) {
        val horaActual = System.currentTimeMillis()
        if (!forzar && horaActual - ultimaHoraObtencion < TIEMPO_ESPERA_MS && _ciudades.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _estaCargando.value = true
            _error.value = null
            try {
                val respuesta = servicioApi.obtenerCiudades()
                if (respuesta.isSuccessful) {
                    _ciudades.value = respuesta.body() ?: emptyList()
                    ultimaHoraObtencion = horaActual
                } else {
                    _error.value = "Error: ${respuesta.code()} ${respuesta.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error de Red: ${e.message}"
            } finally {
                _estaCargando.value = false
            }
        }
    }
}
