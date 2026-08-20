package com.example.codise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ServicioApi
import com.example.codise.data.RespuestaAutenticacion
import com.example.codise.data.SolicitudLogin
import com.example.codise.data.AdministradorSesion
import com.example.codise.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewModelLogin(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)

    private val _estadoUi = MutableStateFlow<EstadoUiLogin>(EstadoUiLogin.Inactivo)
    val estadoUi: StateFlow<EstadoUiLogin> = _estadoUi

    init {
        viewModelScope.launch {
            administradorSesion.sesion.collect { sesion ->
                if (sesion != null) {
                    _estadoUi.value = EstadoUiLogin.Exito(sesion)
                } else {
                    _estadoUi.value = EstadoUiLogin.Inactivo
                }
            }
        }
    }

    fun iniciarSesion(nombreUsuario: String, contrasena: String) {
        viewModelScope.launch {
            _estadoUi.value = EstadoUiLogin.Cargando
            try {
                val respuesta = servicioApi.iniciarSesion(SolicitudLogin(nombreUsuario, contrasena))
                if (respuesta.isSuccessful) {
                    val respuestaAuth = respuesta.body()!!
                    administradorSesion.guardarSesion(respuestaAuth)
                    _estadoUi.value = EstadoUiLogin.Exito(respuestaAuth)
                } else {
                    _estadoUi.value = EstadoUiLogin.Error("Error: ${respuesta.code()} - ${respuesta.message()}")
                }
            } catch (e: Exception) {
                _estadoUi.value = EstadoUiLogin.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun registrar(usuario: Usuario) {
        viewModelScope.launch {
            _estadoUi.value = EstadoUiLogin.Cargando
            try {
                val respuesta = servicioApi.registrarUsuario(usuario)
                if (respuesta.isSuccessful) {
                    val respuestaAuth = respuesta.body()!!
                    administradorSesion.guardarSesion(respuestaAuth)
                    _estadoUi.value = EstadoUiLogin.Exito(respuestaAuth)
                } else {
                    _estadoUi.value = EstadoUiLogin.Error("Error: ${respuesta.code()} - ${respuesta.message()}")
                }
            } catch (e: Exception) {
                _estadoUi.value = EstadoUiLogin.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cerrarSesion() {
        administradorSesion.cerrarSesion()
        _estadoUi.value = EstadoUiLogin.Inactivo
    }
}

sealed class EstadoUiLogin {
    object Inactivo : EstadoUiLogin()
    object Cargando : EstadoUiLogin()
    data class Exito(val respuesta: RespuestaAutenticacion) : EstadoUiLogin()
    data class Error(val mensaje: String) : EstadoUiLogin()
}
