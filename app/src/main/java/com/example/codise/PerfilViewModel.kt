package com.example.codise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ServicioApi
import com.example.codise.data.Empresa
import com.example.codise.data.AdministradorSesion
import com.example.codise.data.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewModelPerfil(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)

    private val _estadoUi = MutableStateFlow<EstadoUiPerfil>(EstadoUiPerfil.Inactivo)
    val estadoUi: StateFlow<EstadoUiPerfil> = _estadoUi

    private val _estadoUiEmpresa = MutableStateFlow<EstadoUiEmpresa>(EstadoUiEmpresa.Inactivo)
    val estadoUiEmpresa: StateFlow<EstadoUiEmpresa> = _estadoUiEmpresa

    fun actualizarPerfil(token: String, usuario: Usuario) {
        viewModelScope.launch {
            _estadoUi.value = EstadoUiPerfil.Cargando
            try {
                val encabezadoAuth = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val respuesta = servicioApi.actualizarPerfil(encabezadoAuth, usuario)
                if (respuesta.isSuccessful) {
                    val usuarioActualizado = respuesta.body()!!
                    
                    administradorSesion.obtenerSesion()?.let { sesionActual ->
                        administradorSesion.guardarSesion(sesionActual.copy(usuario = usuarioActualizado))
                    }
                    
                    _estadoUi.value = EstadoUiPerfil.Exito(usuarioActualizado)
                } else {
                    _estadoUi.value = EstadoUiPerfil.Error("Error: ${respuesta.code()} - ${respuesta.message()}")
                }
            } catch (e: Exception) {
                _estadoUi.value = EstadoUiPerfil.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun reiniciarEstado() {
        _estadoUi.value = EstadoUiPerfil.Inactivo
        _estadoUiEmpresa.value = EstadoUiEmpresa.Inactivo
    }

    fun registrarEmpresa(token: String, empresa: Empresa) {
        viewModelScope.launch {
            _estadoUiEmpresa.value = EstadoUiEmpresa.Cargando
            try {
                val encabezadoAuth = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val respuesta = servicioApi.registrarEmpresa(encabezadoAuth, empresa)
                if (respuesta.isSuccessful) {
                    val empresaRegistrada = respuesta.body()!!
                    
                    administradorSesion.obtenerSesion()?.let { sesionActual ->
                        val usuarioActualizado = sesionActual.usuario.copy(esProtagonista = true)
                        administradorSesion.guardarSesion(sesionActual.copy(usuario = usuarioActualizado))
                        _estadoUi.value = EstadoUiPerfil.Exito(usuarioActualizado)
                    }

                    _estadoUiEmpresa.value = EstadoUiEmpresa.Exito(empresaRegistrada)
                } else {
                    _estadoUiEmpresa.value = EstadoUiEmpresa.Error("Error: ${respuesta.code()} - ${respuesta.message()}")
                }
            } catch (e: Exception) {
                _estadoUiEmpresa.value = EstadoUiEmpresa.Error(e.message ?: "Error desconocido")
            }
        }
    }
}

sealed class EstadoUiPerfil {
    object Inactivo : EstadoUiPerfil()
    object Cargando : EstadoUiPerfil()
    data class Exito(val usuario: Usuario) : EstadoUiPerfil()
    data class Error(val mensaje: String) : EstadoUiPerfil()
}

sealed class EstadoUiEmpresa {
    object Inactivo : EstadoUiEmpresa()
    object Cargando : EstadoUiEmpresa()
    data class Exito(val empresa: Empresa) : EstadoUiEmpresa()
    data class Error(val mensaje: String) : EstadoUiEmpresa()
}
