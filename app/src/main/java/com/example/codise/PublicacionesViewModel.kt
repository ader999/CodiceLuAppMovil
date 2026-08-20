package com.example.codise

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

sealed class EstadoUiPublicaciones {
    object Inactivo : EstadoUiPublicaciones()
    object Cargando : EstadoUiPublicaciones()
    data class Exito(val publicaciones: List<Publicacion>) : EstadoUiPublicaciones()
    data class Error(val mensaje: String) : EstadoUiPublicaciones()
}

class ViewModelPublicaciones(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val servicioApi = ServicioApi.obtenerInstancia(aplicacion)
    private val administradorSesion = AdministradorSesion.obtenerInstancia(aplicacion)

    private val _estadoUi = mutableStateOf<EstadoUiPublicaciones>(EstadoUiPublicaciones.Inactivo)
    val estadoUi: State<EstadoUiPublicaciones> = _estadoUi

    private val _estaSubiendo = mutableStateOf(false)
    val estaSubiendo: State<Boolean> = _estaSubiendo

    private val _subidaExitosa = mutableStateOf(false)
    val subidaExitosa: State<Boolean> = _subidaExitosa

    private val _mensajeError = mutableStateOf<String?>(null)
    val mensajeError: State<String?> = _mensajeError

    init {
        obtenerPublicaciones()
    }

    fun obtenerPublicaciones(
        idEvento: Int? = null,
        idCiudad: Int? = null,
        idEmpresa: Int? = null,
        idAutor: Int? = null
    ) {
        val sesion = administradorSesion.obtenerSesion()
        val token = sesion?.let { "Bearer ${it.tokens.access}" }

        viewModelScope.launch {
            _estadoUi.value = EstadoUiPublicaciones.Cargando
            try {
                val respuesta = servicioApi.obtenerPublicaciones(
                    token = token,
                    idEvento = idEvento,
                    idCiudad = idCiudad,
                    idEmpresa = idEmpresa,
                    idAutor = idAutor
                )
                if (respuesta.isSuccessful) {
                    _estadoUi.value = EstadoUiPublicaciones.Exito(respuesta.body() ?: emptyList())
                } else {
                    _estadoUi.value = EstadoUiPublicaciones.Error("Error: ${respuesta.code()}")
                }
            } catch (e: Exception) {
                _estadoUi.value = EstadoUiPublicaciones.Error("Error de red: ${e.message}")
            }
        }
    }

    fun alternarLike(idPublicacion: Int) {
        val sesion = administradorSesion.obtenerSesion() ?: return
        val token = "Bearer ${sesion.tokens.access}"

        viewModelScope.launch {
            try {
                val respuesta = servicioApi.alternarLike(token, idPublicacion)
                if (respuesta.isSuccessful) {
                    val estadoActual = _estadoUi.value
                    if (estadoActual is EstadoUiPublicaciones.Exito) {
                        val listaActualizada = estadoActual.publicaciones.map {
                            if (it.id == idPublicacion) {
                                val resultado = respuesta.body()!!
                                it.copy(totalLikes = resultado.total_likes, usuarioHaDadoLike = resultado.ha_dado_like)
                            } else it
                        }
                        _estadoUi.value = EstadoUiPublicaciones.Exito(listaActualizada)
                    }
                }
            } catch (e: Exception) {
                // Manejo de error
            }
        }
    }

    fun subirPublicacion(
        descripcion: String,
        idCiudad: Int?,
        idEmpresa: Int?,
        idEvento: Int?,
        urisImagenes: List<Uri>
    ) {
        _mensajeError.value = null
        val sesion = administradorSesion.obtenerSesion()
        if (sesion == null) {
            _mensajeError.value = "Sesión no válida. Por favor, inicia sesión de nuevo."
            return
        }
        val token = "Bearer ${sesion.tokens.access}"

        viewModelScope.launch {
            _estaSubiendo.value = true
            try {
                val cuerpoDescripcion = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
                val cuerpoActiva = "true".toRequestBody("text/plain".toMediaTypeOrNull())

                var imagenPrincipal: MultipartBody.Part? = null
                val imagenesSecundarias = mutableListOf<MultipartBody.Part>()

                urisImagenes.forEachIndexed { indice, uri ->
                    val archivo = obtenerArchivoDeUri(uri)
                    val archivoPeticion = archivo.asRequestBody("image/*".toMediaTypeOrNull())
                    if (indice == 0) {
                        imagenPrincipal = MultipartBody.Part.createFormData("imagen_principal", archivo.name, archivoPeticion)
                    } else {
                        imagenesSecundarias.add(MultipartBody.Part.createFormData("imagenes", archivo.name, archivoPeticion))
                    }
                }

                val respuesta = servicioApi.crearPublicacion(
                    token = token,
                    descripcion = cuerpoDescripcion,
                    idCiudad = idCiudad,
                    idEmpresa = idEmpresa,
                    idEvento = idEvento,
                    estaActiva = cuerpoActiva,
                    imagen_principal = imagenPrincipal,
                    imagenes = imagenesSecundarias.ifEmpty { null }
                )

                if (respuesta.isSuccessful) {
                    _subidaExitosa.value = true
                    obtenerPublicaciones()
                } else {
                    _mensajeError.value = "Error del servidor: ${respuesta.code()} ${respuesta.message()}"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error de red o procesamiento: ${e.localizedMessage}"
            } finally {
                _estaSubiendo.value = false
            }
        }
    }

    private suspend fun obtenerArchivoDeUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val contentResolver = getApplication<Application>().contentResolver
        val flujoEntrada = contentResolver.openInputStream(uri)
        val bitmapOriginal = BitmapFactory.decodeStream(flujoEntrada)
        
        val archivo = File(getApplication<Application>().cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        val flujoSalida = FileOutputStream(archivo)
        
        val escala = if (bitmapOriginal.width > 1280 || bitmapOriginal.height > 1280) {
            val ladoMayor = maxOf(bitmapOriginal.width, bitmapOriginal.height)
            1280f / ladoMayor
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

        bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 80, flujoSalida)
        flujoSalida.flush()
        flujoSalida.close()
        
        if (bitmapFinal != bitmapOriginal) bitmapFinal.recycle()
        bitmapOriginal.recycle()
        
        archivo
    }

    fun reiniciarEstadoSubida() {
        _subidaExitosa.value = false
        _mensajeError.value = null
    }
}
