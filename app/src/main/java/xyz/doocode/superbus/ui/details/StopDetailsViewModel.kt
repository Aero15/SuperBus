package xyz.doocode.superbus.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.core.dto.TempsLieu

sealed interface StopDetailsUiState {
    data object Loading : StopDetailsUiState
    data class Success(
        val tempsLieu: TempsLieu,
        val groupedArrivals: Map<String, List<Temps>> // Key: Ligne + Direction
    ) : StopDetailsUiState

    data class Error(val message: String) : StopDetailsUiState
    data object Empty : StopDetailsUiState
}

class StopDetailsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<StopDetailsUiState>(StopDetailsUiState.Loading)
    val uiState: StateFlow<StopDetailsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var autoRefreshJob: Job? = null
    private var lastRefreshTime = 0L
    private val MIN_REFRESH_INTERVAL = 15_000L // 15 seconds
    private val AUTO_REFRESH_INTERVAL = 30_000L // 30 seconds

    // Identifiers
    private var stopName: String? = null
    private var stopId: String? = null

    fun init(name: String?, id: String?) {
        if (stopName == name && stopId == id && _uiState.value !is StopDetailsUiState.Loading) return

        stopName = name
        stopId = id

        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                loadData(isAutoRefresh = true)
                delay(AUTO_REFRESH_INTERVAL)
            }
        }
    }

    fun refresh() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastRefreshTime < MIN_REFRESH_INTERVAL) {
            // Rate limit triggered, skip refresh
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            loadData(isAutoRefresh = false)
            _isRefreshing.value = false
        }
    }

    private suspend fun loadData(isAutoRefresh: Boolean) {
        if (!isAutoRefresh && _uiState.value is StopDetailsUiState.Loading) {
            // Initial load or explict retry from error state, keep Loading state
        } else if (!isAutoRefresh) {
            // User initiated refresh, UI shows pull indicator, no need to change state to Loading
        }

        try {
            val response = ApiClient.ginkoService.getTempsLieu(
                nom = stopName,
                idArret = stopId,
                nb = 3 // Per API documentation, fetches 3 times per line/direction
            )

            val data = response.objects

            if (data.listeTemps.isEmpty()) {
                _uiState.value = StopDetailsUiState.Empty
            } else {
                // Group by Line and Direction to organize the display
                // Key format example: "3|Centre Ville"
                val grouped = data.listeTemps.groupBy {
                    "${it.numLignePublic}|${it.destination}"
                }

                _uiState.value = StopDetailsUiState.Success(data, grouped)
            }
            lastRefreshTime = System.currentTimeMillis()

        } catch (e: Exception) {
            if (_uiState.value !is StopDetailsUiState.Success) {
                _uiState.value = StopDetailsUiState.Error(
                    e.localizedMessage ?: "Erreur de chargement"
                )
            } else {
                // If we already have data, just log error or show snackbar (not handled here for simplicity)
                // Retain old data
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
    }
}
