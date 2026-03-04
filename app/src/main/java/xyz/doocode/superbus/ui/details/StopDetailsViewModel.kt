package xyz.doocode.superbus.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.core.dto.TempsLieu
import xyz.doocode.superbus.core.manager.FavoritesManager


sealed interface StopDetailsUiState {
    data object Loading : StopDetailsUiState
    data class Success(
        val tempsLieu: TempsLieu?,
        val groupedArrivals: Map<String, List<Temps>>
    ) : StopDetailsUiState

    data class Error(val message: String) : StopDetailsUiState
    data object Empty : StopDetailsUiState
}

class StopDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FavoritesRepository.getInstance(application)
    private val favoritesManager = FavoritesManager(application)

    private val _uiState = MutableStateFlow<StopDetailsUiState>(StopDetailsUiState.Loading)
    val uiState: StateFlow<StopDetailsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private var currentStopName: String? = null
    private var currentStopId: String? = null
    private var detailsFromId: Boolean = true
    private var pollingJob: Job? = null
    private var lastRefreshTime = 0L

    fun init(stopName: String?, stopId: String?, detailsFromId: Boolean = true) {
        val isNew = (currentStopName != stopName || currentStopId != stopId)
        currentStopName = stopName
        currentStopId = stopId
        this.detailsFromId = detailsFromId

        stopId?.let { id ->
            viewModelScope.launch {
                repository.favorites.collectLatest { favorites ->
                    _isFavorite.value = favorites.any { it.id == id }
                }
            }
            viewModelScope.launch {
                favoritesManager.refreshFavoriteLines(id)
            }
        }

        if (isNew) {
            loadData(forceRefresh = true)
            // Récupérer détails de l'arrêt (getDetailsArret) si l'id est présent
        }
        startAutoRefresh()
    }

    private val name: String get() = currentStopName ?: ""
    private val id: String get() = currentStopId ?: ""

    fun toggleFavorite() {
        if (id.isEmpty()) return
        viewModelScope.launch {
            // In details view, we might not have grouped IDs immediately.
            // Using empty list as placeholder, but ideally we should pass real ones.
            // However, this screen is usually accessed from search where grouping happens.
            favoritesManager.toggleFavorite(id, emptyList(), name)
        }
    }

    fun refresh() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < 15000) {
            _isRefreshing.value = false
            return
        }
        lastRefreshTime = now
        loadData(forceRefresh = true)
    }

    private fun startAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                if (!_isRefreshing.value) {
                    loadData(forceRefresh = false)
                }
                delay(30000)
            }
        }
    }

    private fun loadData(forceRefresh: Boolean) {
        if (forceRefresh) _isRefreshing.value = true

        viewModelScope.launch {
            try {
                if (currentStopName != null || currentStopId != null) {
                    val response = when {
                        currentStopId != null && detailsFromId ->
                            ApiClient.ginkoService.getTempsLieu(idArret = currentStopId!!, nb = 3)

                        currentStopName != null && !detailsFromId ->
                            ApiClient.ginkoService.getTempsLieu(nom = currentStopName!!, nb = 3)

                        else -> null
                    }

                    response?.let { res ->
                        val arrivals = res.objects.listeTemps

                        if (arrivals.isEmpty()) {
                            _uiState.value = StopDetailsUiState.Empty
                        } else {
                            val grouped = arrivals
                                .groupBy { "${it.numLignePublic}|${it.destination}" }
                                .mapValues { (_, list) ->
                                    list.sortedWith(compareBy { if (it.temps.contains("min")) 0 else 1 })
                                }

                            _uiState.value = StopDetailsUiState.Success(res.objects, grouped)
                        }
                    }
                } else {
                    _uiState.value =
                        StopDetailsUiState.Error("Aucun identifiant de la station est disponible")
                }
            } catch (e: Exception) {
                if (_uiState.value !is StopDetailsUiState.Success) {
                    _uiState.value = StopDetailsUiState.Error(e.message ?: "Erreur réseau")
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
