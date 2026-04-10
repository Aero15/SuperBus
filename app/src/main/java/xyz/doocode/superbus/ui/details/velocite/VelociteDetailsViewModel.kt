package xyz.doocode.superbus.ui.details.velocite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.dto.jcdecaux.Station

sealed interface VelociteDetailsUiState {
    object Loading : VelociteDetailsUiState
    data class Success(val station: Station) : VelociteDetailsUiState
    data class Error(val message: String) : VelociteDetailsUiState
}

class VelociteDetailsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<VelociteDetailsUiState>(VelociteDetailsUiState.Loading)
    val uiState: StateFlow<VelociteDetailsUiState> = _uiState.asStateFlow()

    private var stationId: Int? = null
    private var pollingJob: Job? = null

    fun setStationId(id: Int) {
        if (stationId == null) {
            stationId = id
            startPolling()
        }
    }

    fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                stationId?.let { id ->
                    try {
                        val station = ApiClient.jcDecauxService.getStation(id)
                        _uiState.value = VelociteDetailsUiState.Success(station)
                    } catch (e: Exception) {
                        if (_uiState.value is VelociteDetailsUiState.Loading) {
                            _uiState.value =
                                VelociteDetailsUiState.Error(e.message ?: "Unknown error")
                        }
                    }
                }
                delay(15_000L) // Refresh every 15 seconds
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun reload() {
        _uiState.value = VelociteDetailsUiState.Loading
        stationId?.let {
            viewModelScope.launch {
                try {
                    val station = ApiClient.jcDecauxService.getStation(it)
                    _uiState.value = VelociteDetailsUiState.Success(station)
                } catch (e: Exception) {
                    _uiState.value = VelociteDetailsUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}
