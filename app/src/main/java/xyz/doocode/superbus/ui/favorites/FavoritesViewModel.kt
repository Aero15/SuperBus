package xyz.doocode.superbus.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.FavoriteStation
import xyz.doocode.superbus.core.util.removeAccents

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoritesRepository.getInstance(application)

    // Search query state
    val searchQuery = MutableStateFlow("")
    val isEditing = MutableStateFlow(false)

    private val _localFavorites = MutableStateFlow<List<FavoriteStation>>(emptyList())

    // Combine repository data with search query or use local list during editing
    val favorites: StateFlow<List<FavoriteStation>> = combine(
        repository.favorites,
        searchQuery,
        isEditing,
        _localFavorites
    ) { repoList, query, editing, localList ->
        if (editing) {
            localList
        } else if (query.isBlank()) {
            repoList
        } else {
            val normalizedQuery = query.trim().removeAccents()
            repoList.filter {
                it.name.removeAccents().contains(normalizedQuery, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun startEditing() {
        // Copy current repo list to local list for editing
        viewModelScope.launch {
            val currentList = repository.favorites.first()
            _localFavorites.value = ArrayList(currentList)
            isEditing.value = true
        }
    }

    fun saveOrder() {
        viewModelScope.launch {
            repository.updateFavoritesOrder(_localFavorites.value)
            isEditing.value = false
        }
    }

    fun cancelEditing() {
        isEditing.value = false
        _localFavorites.value = emptyList()
    }

    fun moveFavorite(fromIndex: Int, toIndex: Int) {
        val currentList = _localFavorites.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _localFavorites.value = currentList
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
