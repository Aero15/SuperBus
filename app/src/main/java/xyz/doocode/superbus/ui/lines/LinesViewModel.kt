package xyz.doocode.superbus.ui.lines

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.ginko.Ligne

data class LinesUiState(
    val isLoading: Boolean = false,
    val lineGroups: Map<String, List<Ligne>> = emptyMap(),
    val error: String? = null,
    val collapsedSections: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isSearching: Boolean = false
)

class LinesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReferenceDataRepository.getInstance(application)
    private val _uiState = MutableStateFlow(LinesUiState())
    val uiState: StateFlow<LinesUiState> = _uiState.asStateFlow()

    // Store all lines to filter locally
    private var allLines: List<Ligne> = emptyList()

    init {
        fetchLines()
    }

    private fun getTypologyLabel(code: Int): String {
        return when (code) {
            10 -> "Tramway"
            20 -> "Lianes"
            30 -> "Lignes urbaines"
            40 -> "Lignes complémentaires"
            50 -> "Lignes périurbaines"
            60 -> "Lignes scolaires"
            70 -> "Services à la demande"
            else -> "Autre"
        }
    }

    private fun fetchLines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Use repository instead of direct API
                allLines = repository.getLignes()

                updateFilteredLines()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Une erreur inconnue est survenue"
                    )
                }
            }
        }
    }

    fun toggleSection(sectionTitle: String) {
        _uiState.update { currentState ->
            val newCollapsed = if (currentState.collapsedSections.contains(sectionTitle)) {
                currentState.collapsedSections - sectionTitle
            } else {
                currentState.collapsedSections + sectionTitle
            }
            currentState.copy(collapsedSections = newCollapsed)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredLines()
    }

    fun toggleSearch() {
        _uiState.update { it.copy(isSearching = !it.isSearching) }
        if (!_uiState.value.isSearching) {
            onSearchQueryChange("")
        }
    }

    private fun updateFilteredLines() {
        val query = _uiState.value.searchQuery.trim().lowercase()

        val filtered = if (query.isEmpty()) {
            allLines
        } else {
            allLines.filter { line ->
                line.numLignePublic.lowercase().contains(query) ||
                        line.libellePublic.lowercase().contains(query) ||
                        line.variantes.any { v ->
                            v.destination.lowercase().contains(query)
                        }
            }
        }

        // Group by typology, handling unknown types as 80 ("Autre")
        val grouped = filtered
            .groupBy { line ->
                if (line.typologie in listOf(10, 20, 30, 40, 50, 60, 70)) line.typologie else 80
            }
            .toSortedMap()
            .mapKeys { (key, _) -> getTypologyLabel(key) }

        _uiState.update { currentState ->
            // If it's the first load (empty groups before), set default collapsed state
            // Logic: Collapse all except the first 3 (indices 0, 1, 2)
            val newCollapsed = if (currentState.lineGroups.isEmpty() && grouped.isNotEmpty()) {
                grouped.keys.drop(3).toSet()
            } else {
                currentState.collapsedSections
            }

            currentState.copy(
                isLoading = false,
                lineGroups = grouped,
                collapsedSections = newCollapsed
            )
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                allLines = repository.getLignes(forceRefresh = true)
                updateFilteredLines()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun retry() {
        fetchLines()
    }
}
