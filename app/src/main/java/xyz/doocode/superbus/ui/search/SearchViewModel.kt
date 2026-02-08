package xyz.doocode.superbus.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.util.removeAccents

sealed interface SearchUiState {
    data object Loading : SearchUiState
    data class Success(val stops: List<Arret>) : SearchUiState
    data class Error(val message: String) : SearchUiState
    data object Empty : SearchUiState // No data from API
}

class SearchViewModel : ViewModel() {

    // Configuration flag for deduplication
    // Set to true to group stops by name, false to show all entries
    val REMOVE_DUPLICATES = true

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
                val response = ApiClient.ginkoService.getArrets()
                var loadedStops = response.objects

                if (REMOVE_DUPLICATES) {
                    loadedStops = loadedStops
                        .distinctBy { it.nom } // Deduplicate by name
                }

                // Sort alphabetically
                loadedStops = loadedStops.sortedBy { it.nom }

                _allStops.value = loadedStops
            } catch (e: Exception) {
                // In a real app, parse the error to give a better message
                _error.value = e.localizedMessage ?: "Une erreur inconnue est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
