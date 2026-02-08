package xyz.doocode.superbus.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.FavoriteStation
import xyz.doocode.superbus.core.util.removeAccents

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoritesRepository.getInstance(application)

    // Search query state
    val searchQuery = MutableStateFlow("")

    // Combine repository data with search query
    val favorites: StateFlow<List<FavoriteStation>> = combine(
        repository.favorites,
        searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val normalizedQuery = query.trim().removeAccents()
            list.filter {
                it.name.removeAccents().contains(normalizedQuery, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
