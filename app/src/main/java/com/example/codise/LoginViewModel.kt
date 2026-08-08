package com.example.codise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ApiService
import com.example.codise.data.AuthResponse
import com.example.codise.data.LoginRequest
import com.example.codise.data.SessionManager
import com.example.codise.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.getInstance(application)
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        val savedSession = sessionManager.getSession()
        if (savedSession != null) {
            _uiState.value = LoginUiState.Success(savedSession)
        }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    sessionManager.saveSession(authResponse)
                    _uiState.value = LoginUiState.Success(authResponse)
                } else {
                    _uiState.value = LoginUiState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(user: User) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = apiService.register(user)
                if (response.isSuccessful) {
                    val authResponse = response.body()!!
                    sessionManager.saveSession(authResponse)
                    _uiState.value = LoginUiState.Success(authResponse)
                } else {
                    _uiState.value = LoginUiState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val response: AuthResponse) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
