package com.example.codise

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ApiService
import com.example.codise.data.City
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val apiService = ApiService.create()

    private val _cities = mutableStateOf<List<City>>(emptyList())
    val cities: State<List<City>> = _cities

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        fetchCities()
    }

    fun fetchCities() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getCities()
                if (response.isSuccessful) {
                    _cities.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Error: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Network Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
