package xyz.doocode.superbus.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class MenuUiState(
    val historyCount: Int = 0,
    val cacheSizeMb: Double = 0.0,
    val isLoadingHistory: Boolean = false,
    val userGreeting: String = "Bonjour, Voyageur !"
)

class MenuViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadHistoryStats()
        refreshGreeting()
    }

    fun refreshGreeting() {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greetings = listOf(
            "Salut",
            "Coucou",
            "Hello",
            "Allons-y",
            "Let's go",
            if (hour in 6..18) "Bonjour" else "Bonsoir"
        )
        _uiState.value = _uiState.value.copy(userGreeting = greetings.random())
    }

    private fun loadHistoryStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingHistory = true)
            // Mock loading data
            delay(1000)
            _uiState.value = _uiState.value.copy(
                isLoadingHistory = false,
                historyCount = 42,
                cacheSizeMb = 12.5
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingHistory = true)
            delay(500) // Mock working
            _uiState.value = _uiState.value.copy(
                isLoadingHistory = false,
                historyCount = 0,
                cacheSizeMb = 0.0
            )
        }
    }
}
