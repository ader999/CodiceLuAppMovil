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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

sealed class PublicationsUiState {
    object Idle : PublicationsUiState()
    object Loading : PublicationsUiState()
    data class Success(val publications: List<Publication>) : PublicationsUiState()
    data class Error(val message: String) : PublicationsUiState()
}

class PublicationsViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.getInstance(application)
    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = mutableStateOf<PublicationsUiState>(PublicationsUiState.Idle)
    val uiState: State<PublicationsUiState> = _uiState

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    private val _uploadSuccess = mutableStateOf(false)
    val uploadSuccess: State<Boolean> = _uploadSuccess

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    init {
        fetchPublications()
    }

    fun fetchPublications(
        eventId: Int? = null,
        cityId: Int? = null,
        businessId: Int? = null,
        authorId: Int? = null
    ) {
        val session = sessionManager.getSession()
        val token = session?.let { "Bearer ${it.tokens.access}" }

        viewModelScope.launch {
            _uiState.value = PublicationsUiState.Loading
            try {
                val response = apiService.getPublications(
                    token = token,
                    eventId = eventId,
                    cityId = cityId,
                    businessId = businessId,
                    authorId = authorId
                )
                if (response.isSuccessful) {
                    _uiState.value = PublicationsUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = PublicationsUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = PublicationsUiState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun toggleLike(publicationId: Int) {
        val session = sessionManager.getSession() ?: return
        val token = "Bearer ${session.tokens.access}"

        viewModelScope.launch {
            try {
                val response = apiService.toggleLike(token, publicationId)
                if (response.isSuccessful) {
                    // Update local state if needed or refresh
                    val currentState = _uiState.value
                    if (currentState is PublicationsUiState.Success) {
                        val updatedList = currentState.publications.map {
                            if (it.id == publicationId) {
                                val result = response.body()!!
                                it.copy(totalLikes = result.total_likes, userHaDadoLike = result.ha_dado_like)
                            } else it
                        }
                        _uiState.value = PublicationsUiState.Success(updatedList)
                    }
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun uploadPublication(
        description: String,
        cityId: Int?,
        businessId: Int?,
        eventId: Int?,
        imageUris: List<Uri>
    ) {
        _errorMessage.value = null
        val session = sessionManager.getSession()
        if (session == null) {
            _errorMessage.value = "Sesión no válida. Por favor, inicia sesión de nuevo."
            return
        }
        val token = "Bearer ${session.tokens.access}"

        viewModelScope.launch {
            _isUploading.value = true
            try {
                val descBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                val activeBody = "true".toRequestBody("text/plain".toMediaTypeOrNull())

                var mainImage: MultipartBody.Part? = null
                val secondaryImages = mutableListOf<MultipartBody.Part>()

                imageUris.forEachIndexed { index, uri ->
                    val file = getFileFromUri(uri)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    if (index == 0) {
                        mainImage = MultipartBody.Part.createFormData("imagen_principal", file.name, requestFile)
                    } else {
                        secondaryImages.add(MultipartBody.Part.createFormData("imagenes", file.name, requestFile))
                    }
                }

                val response = apiService.createPublication(
                    token = token,
                    description = descBody,
                    cityId = cityId,
                    businessId = businessId,
                    eventId = eventId,
                    estaActiva = activeBody,
                    imagen_principal = mainImage,
                    imagenes = secondaryImages.ifEmpty { null }
                )

                if (response.isSuccessful) {
                    _uploadSuccess.value = true
                    fetchPublications()
                } else {
                    _errorMessage.value = "Error del servidor: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de red o procesamiento: ${e.localizedMessage}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    private suspend fun getFileFromUri(uri: Uri): File = withContext(Dispatchers.IO) {
        val contentResolver = getApplication<Application>().contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        
        val file = File(getApplication<Application>().cacheDir, "compressed_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        
        // Compress image to 80% quality and limit max width/height to 1280px
        val scale = if (originalBitmap.width > 1280 || originalBitmap.height > 1280) {
            val maxSide = maxOf(originalBitmap.width, originalBitmap.height)
            1280f / maxSide
        } else {
            1.0f
        }
        
        val finalBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scale).toInt(),
                (originalBitmap.height * scale).toInt(),
                true
            )
        } else {
            originalBitmap
        }

        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        outputStream.flush()
        outputStream.close()
        
        if (finalBitmap != originalBitmap) finalBitmap.recycle()
        originalBitmap.recycle()
        
        file
    }

    fun resetUploadState() {
        _uploadSuccess.value = false
        _errorMessage.value = null
    }
}
