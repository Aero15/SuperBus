package xyz.doocode.superbus.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.core.util.removeAccents

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoritesRepository.getInstance(application)

    // Search query state
    val searchQuery = MutableStateFlow("")
    val isEditing = MutableStateFlow(false)

    private val _localFavorites = MutableStateFlow<List<FavoriteStation>>(emptyList())

    // Undo / Redo stacks — scoped to the current editing session
    private val _undoStack = ArrayDeque<List<FavoriteStation>>()
    private val _redoStack = ArrayDeque<List<FavoriteStation>>()
    val canUndo = MutableStateFlow(false)
    val canRedo = MutableStateFlow(false)

    private fun pushUndo() {
        _undoStack.addLast(_localFavorites.value)
        if (_undoStack.size > 50) _undoStack.removeFirst()
        _redoStack.clear()
        canUndo.value = true
        canRedo.value = false
    }

    /** Call once at the start of a drag gesture to snapshot the pre-drag state. */
    fun captureUndoBeforeDrag() = pushUndo()

    fun undo() {
        if (_undoStack.isEmpty()) return
        _redoStack.addLast(_localFavorites.value)
        _localFavorites.value = _undoStack.removeLast()
        canUndo.value = _undoStack.isNotEmpty()
        canRedo.value = true
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        _undoStack.addLast(_localFavorites.value)
        _localFavorites.value = _redoStack.removeLast()
        canUndo.value = true
        canRedo.value = _redoStack.isNotEmpty()
    }

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

    fun renameFavorite(stopId: String, detailsFromId: Boolean, newName: String) {
        viewModelScope.launch {
            repository.renameFavorite(stopId, detailsFromId, newName)
        }
    }

    fun removeFavorite(stopId: String, detailsFromId: Boolean) {
        viewModelScope.launch {
            repository.removeFavorite(stopId, detailsFromId)
        }
    }

    // --- Selection state (edit mode only) ---
    val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    private fun FavoriteStation.selectionKey() = "${id}_${detailsFromId}"

    fun toggleSelection(station: FavoriteStation) {
        val key = station.selectionKey()
        val current = selectedIds.value
        selectedIds.value = if (key in current) current - key else current + key
    }

    fun selectAll() {
        selectedIds.value = _localFavorites.value.map { it.selectionKey() }.toSet()
    }

    fun invertSelection() {
        val all = _localFavorites.value.map { it.selectionKey() }.toSet()
        selectedIds.value = all - selectedIds.value
    }

    fun deleteSelected() {
        pushUndo()
        val keys = selectedIds.value
        _localFavorites.value = _localFavorites.value.filter { it.selectionKey() !in keys }
        selectedIds.value = emptySet()
    }

    fun renameInEditMode(station: FavoriteStation, newName: String) {
        pushUndo()
        _localFavorites.value = _localFavorites.value.map {
            if (it.selectionKey() == station.selectionKey()) it.copy(name = newName) else it
        }
    }

    fun startEditing() {
        viewModelScope.launch {
            val currentList = repository.favorites.first()
            _localFavorites.value = ArrayList(currentList)
            selectedIds.value = emptySet()
            _undoStack.clear(); _redoStack.clear()
            canUndo.value = false; canRedo.value = false
            isEditing.value = true
        }
    }

    fun startEditingWithSelection(station: FavoriteStation) {
        viewModelScope.launch {
            val currentList = repository.favorites.first()
            _localFavorites.value = ArrayList(currentList)
            selectedIds.value = setOf(station.selectionKey())
            _undoStack.clear(); _redoStack.clear()
            canUndo.value = false; canRedo.value = false
            isEditing.value = true
        }
    }

    fun saveOrder() {
        viewModelScope.launch {
            repository.updateFavoritesOrder(_localFavorites.value)
            isEditing.value = false
            selectedIds.value = emptySet()
            _undoStack.clear(); _redoStack.clear()
            canUndo.value = false; canRedo.value = false
        }
    }

    fun cancelEditing() {
        isEditing.value = false
        _localFavorites.value = emptyList()
        selectedIds.value = emptySet()
        _undoStack.clear(); _redoStack.clear()
        canUndo.value = false; canRedo.value = false
    }

    fun moveFavorite(fromIndex: Int, toIndex: Int) {
        val currentList = _localFavorites.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            _localFavorites.value = currentList
        }
    }

    fun moveSelectedFavorites(anchorItem: FavoriteStation, toIndex: Int) {
        val currentList = _localFavorites.value.toMutableList()
        val selKeys = selectedIds.value
        if (selKeys.size <= 1) {
            val fromIndex =
                currentList.indexOfFirst { it.selectionKey() == anchorItem.selectionKey() }
            moveFavorite(fromIndex, toIndex)
            return
        }
        val selectedInOrder = currentList.filter { it.selectionKey() in selKeys }
        val notSelected = currentList.filter { it.selectionKey() !in selKeys }
        // Map toIndex (in full list) to an insert position within notSelected
        val insertAt = currentList.take((toIndex + 1).coerceAtMost(currentList.size))
            .count { it.selectionKey() !in selKeys }
            .coerceIn(0, notSelected.size)
        val result = notSelected.toMutableList()
        result.addAll(insertAt, selectedInOrder)
        _localFavorites.value = result
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }
}
