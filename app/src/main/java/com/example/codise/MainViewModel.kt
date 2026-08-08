package com.example.codise

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codise.data.ApiService
import com.example.codise.data.City
import com.example.codise.data.Circuit
import com.example.codise.data.SessionManager
import com.example.codise.data.VisitRequest
import com.example.codise.data.local.AppDatabase
import com.example.codise.data.local.VisitedPoi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = ApiService.getInstance(application)
    private val sessionManager = SessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val visitedPoiDao = database.visitedPoiDao()

    val visitedPoiIds = visitedPoiDao.getAllVisited()
        .map { list -> list.map { it.poiId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val visitedPois = visitedPoiDao.getAllVisited()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cities = mutableStateOf<List<City>>(emptyList())
    val cities: State<List<City>> = _cities

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _selectedCityId = mutableStateOf<Int?>(null)
    val selectedCity: City?
        get() = cities.value.find { it.id == _selectedCityId.value }

    private val _selectedCircuitId = mutableStateOf<Int?>(null)
    val selectedCircuitId: Int? get() = _selectedCircuitId.value

    val selectedCircuit: Circuit?
        get() = selectedCity?.circuitos?.find { it.id == _selectedCircuitId.value }

    private var lastFetchTime = 0L
    private val COOLDOWN_MS = 5 * 60 * 1000 // 5 minutes

    init {
        fetchCities()
        syncVisitedPois()
    }

    fun syncVisitedPois() {
        val session = sessionManager.getSession() ?: return
        val token = "Bearer ${session.tokens.access}"

        viewModelScope.launch {
            try {
                val response = apiService.getVisits(token)
                if (response.isSuccessful) {
                    val cloudVisits = response.body() ?: emptyList()
                    visitedPoiDao.deleteAll()
                    visitedPoiDao.insertAll(cloudVisits.map { 
                        VisitedPoi(it.poiId, isValidated = it.esValidada) 
                    })
                }
            } catch (e: Exception) {
                // Network error or other issues, fallback to local data which is already there
                e.printStackTrace()
            }
        }
    }

    fun selectCity(id: Int?) {
        _selectedCityId.value = id
        _selectedCircuitId.value = null // Clear circuit when city changes
    }

    fun selectCircuit(id: Int?) {
        _selectedCircuitId.value = id
    }

    fun toggleVisited(poiId: Int, lat: Double? = null, lng: Double? = null) {
        viewModelScope.launch {
            val isCurrentlyVisited = visitedPoiIds.value.contains(poiId)
            
            // Local update first for responsiveness
            if (isCurrentlyVisited) {
                visitedPoiDao.unmarkAsVisited(VisitedPoi(poiId))
            } else {
                visitedPoiDao.markAsVisited(VisitedPoi(poiId))
            }

            // Sync with cloud if logged in
            val session = sessionManager.getSession()
            if (session != null && !isCurrentlyVisited) {
                val token = "Bearer ${session.tokens.access}"
                try {
                    val visitRequest = VisitRequest(poiId, lat, lng)
                    val response = apiService.registerVisit(token, visitRequest)
                    if (response.isSuccessful) {
                        val visitResponse = response.body()
                        if (visitResponse != null && visitResponse.esValidada) {
                            // Update local record with validation status
                            visitedPoiDao.markAsVisited(VisitedPoi(poiId, isValidated = true))
                        }
                    } else {
                        _error.value = "Cloud sync failed: ${response.code()}"
                    }
                } catch (e: Exception) {
                    _error.value = "Cloud sync error: ${e.message}"
                }
            }
        }
    }

    fun fetchCities(force: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        if (!force && currentTime - lastFetchTime < COOLDOWN_MS && _cities.value.isNotEmpty()) {
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getCities()
                if (response.isSuccessful) {
                    _cities.value = response.body() ?: emptyList()
                    lastFetchTime = currentTime
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
