package xyz.doocode.superbus.ui.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.api.GinkoResponse
import xyz.doocode.superbus.core.dto.Ligne

data class LinesUiState(
    val isLoading: Boolean = false,
    val lineGroups: Map<String, List<Ligne>> = emptyMap(),
    val error: String? = null
)

class LinesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LinesUiState())
    val uiState: StateFlow<LinesUiState> = _uiState.asStateFlow()

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
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Assuming ApiClient.ginkoService returns a GinkoResponse<List<Ligne>>
                val response: GinkoResponse<List<Ligne>> = ApiClient.ginkoService.getLignes()

                // Group by typology, handling unknown types as 80 ("Autre")
                val grouped = response.objects
                    .groupBy { line ->
                        if (line.typologie in listOf(
                                10,
                                20,
                                30,
                                40,
                                50,
                                60,
                                70
                            )
                        ) line.typologie else 80
                    }
                    .toSortedMap()
                    .mapKeys { (key, _) -> getTypologyLabel(key) }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lineGroups = grouped
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Une erreur inconnue est survenue"
                )
            }
        }
    }

    fun retry() {
        fetchLines()
    }
}
