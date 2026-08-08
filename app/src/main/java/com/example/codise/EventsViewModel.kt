package com.example.codise

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ApiService
import com.example.codise.data.Event
import com.example.codise.data.EventRequest
import com.example.codise.data.SessionManager
import kotlinx.coroutines.launch

sealed class EventsUiState {
    object Idle : EventsUiState()
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.getInstance(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = mutableStateOf<EventsUiState>(EventsUiState.Idle)
    val uiState: State<EventsUiState> = _uiState

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    private val _uploadSuccess = mutableStateOf(false)
    val uploadSuccess: State<Boolean> = _uploadSuccess

    init {
        fetchEvents()
    }

    fun fetchEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            try {
                val response = apiService.getEvents()
                if (response.isSuccessful) {
                    _uiState.value = EventsUiState.Success(response.body() ?: emptyList())
                } else {
                    _uiState.value = EventsUiState.Error("Error: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = EventsUiState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun uploadEvent(eventRequest: EventRequest) {
        val session = sessionManager.getSession() ?: return
        val token = "Bearer ${session.tokens.access}"

        viewModelScope.launch {
            _isUploading.value = true
            try {
                val response = apiService.createEvent(token, eventRequest)
                if (response.isSuccessful) {
                    _uploadSuccess.value = true
                    fetchEvents() // Refresh list
                } else {
                    // Handle error
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun resetUploadState() {
        _uploadSuccess.value = false
    }
}
