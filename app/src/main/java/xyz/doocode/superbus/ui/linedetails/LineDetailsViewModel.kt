package xyz.doocode.superbus.ui.linedetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.dto.Ligne
import xyz.doocode.superbus.core.dto.Variante

data class LineDetailsUiState(
    val line: Ligne? = null,
    val selectedVariante: Variante? = null,
    val stops: List<Arret> = emptyList(),
    val filteredStops: List<Arret> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val isSearching: Boolean = false
)

class LineDetailsViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ReferenceDataRepository.getInstance(application)
    private val _uiState = MutableStateFlow(LineDetailsUiState())
    val uiState: StateFlow<LineDetailsUiState> = _uiState.asStateFlow()

    init {
        val lineJson = savedStateHandle.get<String>("EXTRA_LINE_JSON")
        val variantId = savedStateHandle.get<String>("EXTRA_VARIANT_ID")

        if (lineJson != null) {
            val line = Gson().fromJson(lineJson, Ligne::class.java)
            val initialVariant =
                line.variantes.find { it.id == variantId } ?: line.variantes.firstOrNull()

            _uiState.update {
                it.copy(
                    line = line,
                    selectedVariante = initialVariant
                )
            }

            if (initialVariant != null) {
                fetchStops(line.id, initialVariant.id)
            }
        }
    }

    fun onVariantSelected(variante: Variante) {
        val currentLineId = _uiState.value.line?.id ?: return
        _uiState.update { it.copy(selectedVariante = variante) }
        fetchStops(currentLineId, variante.id)
    }

    private fun fetchStops(lineId: String, variantId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val stops = repository.getDetailsVariante(lineId, variantId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        stops = stops,
                    )
                }
                updateFilteredStops()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Erreur chargement des stations"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateFilteredStops()
    }

    fun toggleSearch() {
        _uiState.update { newState ->
            val newIsSearching = !newState.isSearching
            if (!newIsSearching) {
                // Clear query if closing search
                newState.copy(isSearching = false, searchQuery = "")
            } else {
                newState.copy(isSearching = true)
            }
        }
        if (!_uiState.value.isSearching) updateFilteredStops()
    }

    private fun updateFilteredStops() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val allStops = _uiState.value.stops

        val filtered = if (query.isEmpty()) {
            allStops
        } else {
            allStops.filter { stop ->
                stop.nom.lowercase().contains(query)
            }
        }

        _uiState.update { it.copy(filteredStops = filtered) }
    }
}
