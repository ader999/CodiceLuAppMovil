package com.example.codise

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ApiService
import com.example.codise.data.Business
import com.example.codise.data.SessionManager
import com.example.codise.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.getInstance(application)
    private val sessionManager = SessionManager.getInstance(application)

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _businessUiState = MutableStateFlow<BusinessUiState>(BusinessUiState.Idle)
    val businessUiState: StateFlow<BusinessUiState> = _businessUiState

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
        _businessUiState.value = BusinessUiState.Idle
    }

    fun registerBusiness(token: String, business: Business) {
        viewModelScope.launch {
            _businessUiState.value = BusinessUiState.Loading
            try {
                val authHeader = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = apiService.registerBusiness(authHeader, business)
                if (response.isSuccessful) {
                    val registeredBusiness = response.body()!!
                    
                    // After successful business registration, we might want to refresh the user profile
                    // because the user is now a "protagonista".
                    // The API doesn't seem to return the updated user in the business response,
                    // so we might need a separate call or just update the local session state manually if we trust the API.
                    
                    sessionManager.getSession()?.let { currentSession ->
                        val updatedUser = currentSession.user.copy(esProtagonista = true)
                        sessionManager.saveSession(currentSession.copy(user = updatedUser))
                        // Also trigger a profile success state to update UI
                        _uiState.value = ProfileUiState.Success(updatedUser)
                    }

                    _businessUiState.value = BusinessUiState.Success(registeredBusiness)
                } else {
                    _businessUiState.value = BusinessUiState.Error("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _businessUiState.value = BusinessUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class BusinessUiState {
    object Idle : BusinessUiState()
    object Loading : BusinessUiState()
    data class Success(val business: Business) : BusinessUiState()
    data class Error(val message: String) : BusinessUiState()
}
