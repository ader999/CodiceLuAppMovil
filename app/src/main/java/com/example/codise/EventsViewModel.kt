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
import com.example.codise.utils.NetworkUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class EventsUiState {
    object Idle : EventsUiState()
    object Loading : EventsUiState()
    data class Success(val events: List<Event>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

class EventsViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.getInstance(application)
    private val sessionManager = SessionManager.getInstance(application)
    private val networkUtils = NetworkUtils(application)

    private val _uiState = mutableStateOf<EventsUiState>(EventsUiState.Idle)
    val uiState: State<EventsUiState> = _uiState

    private val _isUploading = mutableStateOf(false)
    val isUploading: State<Boolean> = _isUploading

    private val _uploadSuccess = mutableStateOf(false)
    val uploadSuccess: State<Boolean> = _uploadSuccess

    init {
        fetchEvents()
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            networkUtils.isConnected.collectLatest { isConnected ->
                if (isConnected) {
                    syncPendingAttendance()
                }
            }
        }
    }

    private fun syncPendingAttendance() {
        val pending = sessionManager.getPendingAttendance()
        if (pending.isNotEmpty()) {
            pending.forEach { eventId ->
                registerAttendance(eventId)
            }
        }
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

    fun registerAttendance(eventId: Int) {
        val session = sessionManager.getSession() ?: return
        val token = "Bearer ${session.tokens.access}"

        if (!networkUtils.hasInternet()) {
            sessionManager.addPendingAttendance(eventId)
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.registerAttendance(token, eventId)
                if (response.isSuccessful) {
                    sessionManager.removePendingAttendance(eventId)
                } else {
                    sessionManager.addPendingAttendance(eventId)
                }
            } catch (e: Exception) {
                sessionManager.addPendingAttendance(eventId)
            }
        }
    }
}
