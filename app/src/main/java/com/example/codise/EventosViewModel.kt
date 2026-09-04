package com.example.codise

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ServicioApi
import com.example.codise.data.Evento
import com.example.codise.data.SolicitudEvento
import com.example.codise.data.AdministradorSesion
import com.example.codise.utils.UtilidadesRed
import com.example.codise.utils.obtenerArchivoComprimidoDeUri
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    fun subirEvento(solicitudEvento: SolicitudEvento, uriImagen: Uri? = null) {
        val sesion = administradorSesion.obtenerSesion() ?: return
        val token = "Bearer ${sesion.tokens.access}"

        viewModelScope.launch {
            _estaSubiendo.value = true
            try {
                val respuesta = if (uriImagen != null) {
                    val context = getApplication<Application>()
                    val archivo = context.obtenerArchivoComprimidoDeUri(uriImagen)
                    val archivoPeticion = archivo.asRequestBody("image/*".toMediaTypeOrNull())
                    val parteImagen = MultipartBody.Part.createFormData("imagen", archivo.name, archivoPeticion)

                    val textMedia = "text/plain".toMediaTypeOrNull()
                    servicioApi.crearEventoMultipart(
                        token = token,
                        titulo = solicitudEvento.titulo.toRequestBody(textMedia),
                        descripcion = solicitudEvento.descripcion.toRequestBody(textMedia),
                        ciudad = solicitudEvento.ciudad.toString().toRequestBody(textMedia),
                        empresa = solicitudEvento.empresa?.toString()?.toRequestBody(textMedia),
                        fechaInicio = solicitudEvento.fechaInicio.toRequestBody(textMedia),
                        fechaFin = solicitudEvento.fechaFin.toRequestBody(textMedia),
                        ubicacion = solicitudEvento.ubicacion.toRequestBody(textMedia),
                        precioEntrada = solicitudEvento.precioEntrada.toRequestBody(textMedia),
                        esGratuito = solicitudEvento.esGratuito.toString().toRequestBody(textMedia),
                        cupoMaximo = solicitudEvento.cupoMaximo?.toString()?.toRequestBody(textMedia),
                        latitud = solicitudEvento.latitud?.toString()?.toRequestBody(textMedia),
                        longitud = solicitudEvento.longitud?.toString()?.toRequestBody(textMedia),
                        estaActivo = solicitudEvento.estaActivo.toString().toRequestBody(textMedia),
                        imagen = parteImagen
                    )
                } else {
                    servicioApi.crearEvento(token, solicitudEvento)
                }

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
