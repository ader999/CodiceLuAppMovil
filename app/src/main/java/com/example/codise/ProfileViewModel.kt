package com.example.codise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ApiService
import com.example.codise.data.SessionManager
import com.example.codise.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.create()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun updateProfile(token: String, user: User) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                // Ensure token has "Bearer " prefix if required by the API
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = apiService.updateProfile(authHeader, user)
                if (response.isSuccessful) {
                    val updatedUser = response.body()!!
                    
                    // Update session manager with the new user data while keeping tokens
                    sessionManager.getSession()?.let { currentSession ->
                        sessionManager.saveSession(currentSession.copy(user = updatedUser))
                    }
                    
                    _uiState.value = ProfileUiState.Success(updatedUser)
                } else {
                    _uiState.value = ProfileUiState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}
