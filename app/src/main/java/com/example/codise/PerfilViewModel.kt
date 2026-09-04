package com.example.codise

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ServicioApi
import com.example.codise.data.Empresa
import com.example.codise.data.AdministradorSesion
import com.example.codise.data.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class ViewModelPerfil(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)

    private val _estadoUi = MutableStateFlow<EstadoUiPerfil>(EstadoUiPerfil.Inactivo)
    val estadoUi: StateFlow<EstadoUiPerfil> = _estadoUi

    private val _estadoUiEmpresa = MutableStateFlow<EstadoUiEmpresa>(EstadoUiEmpresa.Inactivo)
    val estadoUiEmpresa: StateFlow<EstadoUiEmpresa> = _estadoUiEmpresa

    fun actualizarPerfil(token: String, usuario: Usuario, uriFoto: Uri? = null) {
        viewModelScope.launch {
            _estadoUi.value = EstadoUiPerfil.Cargando
            try {
                val encabezadoAuth = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val respuesta = if (uriFoto != null) {
                    val archivo = obtenerArchivoDeUri(uriFoto)
                    val archivoPeticion = archivo.asRequestBody("image/*".toMediaTypeOrNull())
                    val parteFoto = MultipartBody.Part.createFormData("foto_perfil", archivo.name, archivoPeticion)
                    
                    val nombreBody = usuario.nombre?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val apellidoBody = usuario.apellido?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val usuarioBody = usuario.nombreUsuario?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val correoBody = usuario.correoElectronico?.toRequestBody("text/plain".toMediaTypeOrNull())
                    val telefonoBody = usuario.telefono?.toRequestBody("text/plain".toMediaTypeOrNull())

                    servicioApi.actualizarPerfilMultipart(
                        token = encabezadoAuth,
                        nombre = nombreBody,
                        apellido = apellidoBody,
                        nombreUsuario = usuarioBody,
                        correoElectronico = correoBody,
                        telefono = telefonoBody,
                        foto_perfil = parteFoto
                    )
                } else {
                    servicioApi.actualizarPerfil(encabezadoAuth, usuario)
                }

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

    fun actualizarFotoPerfil(token: String, uri: Uri) {
        viewModelScope.launch {
            _estadoUi.value = EstadoUiPerfil.Cargando
            try {
                val archivo = obtenerArchivoDeUri(uri)
                val archivoPeticion = archivo.asRequestBody("image/*".toMediaTypeOrNull())
                val parteFoto = MultipartBody.Part.createFormData("foto_perfil", archivo.name, archivoPeticion)
                val encabezadoAuth = if (token.startsWith("Bearer ")) token else "Bearer $token"

                val respuesta = servicioApi.actualizarFotoPerfil(encabezadoAuth, parteFoto)
                if (respuesta.isSuccessful) {
                    val usuarioActualizado = respuesta.body()!!
                    administradorSesion.obtenerSesion()?.let { sesionActual ->
                        administradorSesion.guardarSesion(sesionActual.copy(usuario = usuarioActualizado))
                    }
                    _estadoUi.value = EstadoUiPerfil.Exito(usuarioActualizado)
                } else {
                    _estadoUi.value = EstadoUiPerfil.Error("Error al actualizar la foto: ${respuesta.code()} - ${respuesta.message()}")
                }
            } catch (e: Exception) {
                _estadoUi.value = EstadoUiPerfil.Error(e.message ?: "Error al procesar la imagen")
            }
        }
    }

    private suspend fun obtenerArchivoDeUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val contentResolver = getApplication<Application>().contentResolver
        val flujoEntrada = contentResolver.openInputStream(uri)
        val bitmapOriginal = BitmapFactory.decodeStream(flujoEntrada)
        
        val archivo = File(getApplication<Application>().cacheDir, "perfil_${System.currentTimeMillis()}.jpg")
        val flujoSalida = FileOutputStream(archivo)
        
        val escala = if (bitmapOriginal.width > 1024 || bitmapOriginal.height > 1024) {
            val ladoMayor = maxOf(bitmapOriginal.width, bitmapOriginal.height)
            1024f / ladoMayor
        } else {
            1.0f
        }
        
        val bitmapFinal = if (escala < 1.0f) {
            Bitmap.createScaledBitmap(
                bitmapOriginal,
                (bitmapOriginal.width * escala).toInt(),
                (bitmapOriginal.height * escala).toInt(),
                true
            )
        } else {
            bitmapOriginal
        }

        bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 85, flujoSalida)
        flujoSalida.flush()
        flujoSalida.close()
        
        if (bitmapFinal != bitmapOriginal) bitmapFinal.recycle()
        bitmapOriginal.recycle()
        
        archivo
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
