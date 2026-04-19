package xyz.doocode.superbus.ui.details.velocite

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.core.manager.FavoritesManager
import xyz.doocode.superbus.core.util.formatVelociteStationName

sealed interface VelociteDetailsUiState {
    object Loading : VelociteDetailsUiState
    data class Success(val station: Station) : VelociteDetailsUiState
    data class Error(val message: String) : VelociteDetailsUiState
}

class VelociteDetailsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow<VelociteDetailsUiState>(VelociteDetailsUiState.Loading)
    val uiState: StateFlow<VelociteDetailsUiState> = _uiState.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _nearbyStops = MutableStateFlow<List<Arret>>(emptyList())
    val nearbyStops: StateFlow<List<Arret>> = _nearbyStops.asStateFlow()

    private val _isLoadingNearbyStops = MutableStateFlow(false)
    val isLoadingNearbyStops: StateFlow<Boolean> = _isLoadingNearbyStops.asStateFlow()

    private val favoritesManager = FavoritesManager(application)
    private val favoritesRepository = FavoritesRepository.getInstance(application)

    private var stationId: Int? = null
    private var stationName: String = ""
    private var pollingJob: Job? = null
    private var nearbyStopsLoaded = false

    fun setStationId(id: Int, name: String) {
        if (stationId == null) {
            stationId = id
            stationName = formatVelociteStationName(name)
            observeFavoriteState(id.toString())
            startPolling()
        }
    }

    private fun observeFavoriteState(idStr: String) {
        viewModelScope.launch {
            favoritesRepository.favorites.collectLatest { list ->
                _isFavorite.value = list.any {
                    it.id == idStr && it.effectiveKind == FavoriteStation.KIND_VELOCITE
                }
            }
        }
    }

    fun toggleFavorite() {
        val id = stationId ?: return
        viewModelScope.launch {
            favoritesManager.toggleFavoriteVelocite(id.toString(), stationName)
        }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (true) {
                stationId?.let { id ->
                    try {
                        val station = ApiClient.jcDecauxService.getStation(id)
                        _uiState.value = VelociteDetailsUiState.Success(station)
                        if (!nearbyStopsLoaded) {
                            loadNearbyStops(station)
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        if (_uiState.value is VelociteDetailsUiState.Loading) {
                            _uiState.value =
                                VelociteDetailsUiState.Error(e.message ?: "Unknown error")
                        }
                    }
                }
                delay(15_000L)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun reload() {
        _uiState.value = VelociteDetailsUiState.Loading
        nearbyStopsLoaded = false
        stationId?.let {
            viewModelScope.launch {
                try {
                    val station = ApiClient.jcDecauxService.getStation(it)
                    _uiState.value = VelociteDetailsUiState.Success(station)
                    loadNearbyStops(station, forceRefresh = true)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    _uiState.value = VelociteDetailsUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    private fun loadNearbyStops(station: Station, forceRefresh: Boolean = false) {
        if (nearbyStopsLoaded && !forceRefresh) return

        viewModelScope.launch {
            _isLoadingNearbyStops.value = true
            try {
                val rawNearby = ApiClient.ginkoService.getArretsProches(
                    latitude = station.position.latitude,
                    longitude = station.position.longitude
                ).objects

                val grouped = rawNearby
                    .groupBy { it.nom }
                    .map { (_, stops) ->
                        val sorted = stops.sortedBy { it.id }
                        sorted.first().copy(duplicates = sorted)
                    }

                _nearbyStops.value = grouped
                nearbyStopsLoaded = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _nearbyStops.value = emptyList()
            } finally {
                _isLoadingNearbyStops.value = false
            }
        }
    }
}
