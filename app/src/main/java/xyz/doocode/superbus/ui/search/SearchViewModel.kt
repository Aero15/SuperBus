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
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.manager.FavoritesManager
import xyz.doocode.superbus.core.util.removeAccents

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val stops: List<Arret>) : SearchUiState
    data class Error(val message: String) : SearchUiState
    data object Empty : SearchUiState // No data from API
}

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val favoritesManager = FavoritesManager(application)
    private val referenceDataRepository = ReferenceDataRepository.getInstance(application)

    // Configuration flag for grouping duplicates
    // Set to true to show grouped stops with a badge, false to show expanded view
    val GROUP_DUPLICATES = true

    private val _allStops = MutableStateFlow<List<Arret>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<SearchUiState> = combine(
        _allStops,
        _isLoading,
        _error,
        searchQuery
    ) { stops, isLoading, error, query ->
        if (isLoading) {
            SearchUiState.Loading
        } else if (error != null) {
            SearchUiState.Error(error)
        } else if (stops.isEmpty()) {
            SearchUiState.Empty
        } else {
            val filteredStops = if (query.isBlank()) {
                stops
            } else {
                val normalizedQuery = query.trim().removeAccents()
                stops.filter {
                    it.nom.removeAccents().contains(normalizedQuery, ignoreCase = true)
                }
            }
            SearchUiState.Success(filteredStops)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState.Loading
    )

    init {
        loadStops()
    }

    fun loadStops() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Use repository to get cached data if available
                val rawStops = referenceDataRepository.getArrets()

                // Always group duplicates by name to prevent data loss
                val groupedStopsMap = rawStops.groupBy { it.nom }
                val processedStops = groupedStopsMap.map { (_, stops) ->
                    // Use a consistent sorting strategy (e.g., by ID) or keep original order
                    // Here we sort by ID to ensure the main stop is deterministic
                    val sortedStops = stops.sortedBy { it.id }
                    val mainStop = sortedStops.first()

                    // Create a copy with the duplicates list populated
                    // This includes the main stop itself in the list
                    mainStop.copy(duplicates = sortedStops).also {
                        // Automatically update grouped IDs in favorites if needed
                        if (favoritesManager.isFavorite(it.id)) {
                            favoritesManager.updateFavoriteGroupedIds(it.id, it.groupedIds)
                        }
                    }
                }

                // Sort alphabetically
                val sortedList = processedStops.sortedBy { it.nom }

                _allStops.value = sortedList
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Une erreur inconnue est survenue"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun toggleFavorite(stop: Arret) {
        viewModelScope.launch {
            favoritesManager.toggleFavorite(stop.id, stop.groupedIds, stop.nom)
        }
    }
}
