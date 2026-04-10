package xyz.doocode.superbus.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.core.manager.FavoritesManager
import xyz.doocode.superbus.core.util.formatVelociteStationName
import xyz.doocode.superbus.core.util.removeAccents

sealed interface SearchResult {
    data class Stop(val arret: Arret, val displayName: String) : SearchResult
    data class VeloStation(val station: Station, val displayName: String) : SearchResult
}

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(
        val stops: List<Arret>, val stations: List<Station>, val merged: List<SearchResult>
    ) : SearchUiState

    data class Error(val message: String) : SearchUiState
    data object Empty : SearchUiState // No data from API
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)
    private val referenceDataRepository = ReferenceDataRepository.getInstance(application)
    private val jcDecauxService = ApiClient.jcDecauxService

    // Configuration flag for grouping duplicates
    // Set to true to show grouped stops with a badge, false to show expanded view
    val GROUP_DUPLICATES = true

    private val _allStops = MutableStateFlow<List<Arret>>(emptyList())
    private val _allStations = MutableStateFlow<List<Station>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        _allStops,
        _allStations,
        _isLoading,
        _error,
        searchQuery
    ) { stops, stations, isLoading, error, query ->
        if (isLoading) {
            SearchUiState.Loading
        } else if (error != null) {
            SearchUiState.Error(error)
        } else if (stops.isEmpty() && stations.isEmpty()) {
            SearchUiState.Empty
        } else {
            val filteredStops = if (query.isBlank()) {
                stops
            } else {
                val normalizedQuery = query.trim().removeAccents()
                stops.filter { stop ->
                    stop.nom.removeAccents()
                        .contains(normalizedQuery, ignoreCase = true) || stop.duplicates.any {
                        it.id.contains(
                            normalizedQuery, ignoreCase = true
                        )
                    } || (stop.duplicates.isEmpty() && stop.id.contains(
                        normalizedQuery, ignoreCase = true
                    ))
                }
            }
            val filteredStations = if (query.isBlank()) {
                stations
            } else {
                val normalizedQuery = query.trim().removeAccents()
                stations.filter { station ->
                    formatVelociteStationName(station.name).removeAccents()
                        .contains(normalizedQuery, ignoreCase = true) || station.number.toString()
                        .contains(
                            normalizedQuery, ignoreCase = true
                        )
                }
            }
            val mergedResults: List<SearchResult> = (filteredStops.map { stop ->
                SearchResult.Stop(stop, stop.nom)
            } + filteredStations.map { station ->
                SearchResult.VeloStation(
                    station, formatVelociteStationName(
                        station.name
                    )
                )
            }).sortedBy { result ->
                when (result) {
                    is SearchResult.Stop -> result.displayName

                    is SearchResult.VeloStation -> result.displayName
                }.removeAccents().uppercase()
            }
            SearchUiState.Success(filteredStops, filteredStations, mergedResults)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Loading
    )

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val rawStops = referenceDataRepository.getArrets()
                val groupedStopsMap = rawStops.groupBy { it.nom }
                val processedStops = groupedStopsMap.map { (_, stops) ->
                    val sortedStops = stops.sortedBy { it.id }
                    val mainStop = sortedStops.first()
                    mainStop.copy(duplicates = sortedStops)
                }
                val sortedList = processedStops.sortedBy { it.nom }
                _allStops.value = sortedList
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Une erreur inconnue est survenue (Ginko)"
                e.printStackTrace()
            }

            try {
                _allStations.value = jcDecauxService.getStations().sortedBy {
                    formatVelociteStationName(it.name)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _isLoading.value = false
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun toggleFavorite(stop: Arret, detailsFromId: Boolean? = null) {
        viewModelScope.launch {
            val actualDetailsFromId =
                detailsFromId ?: !(GROUP_DUPLICATES && stop.duplicates.size > 1)
            favoritesManager.toggleFavorite(stop.id, stop.nom, actualDetailsFromId)
        }
    }
}
