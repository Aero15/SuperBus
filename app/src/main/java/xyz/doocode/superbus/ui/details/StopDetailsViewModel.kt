package xyz.doocode.superbus.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.Normalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.api.ApiClient
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.ginko.Temps
import xyz.doocode.superbus.core.dto.ginko.TempsLieu
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.core.manager.FavoritesManager
import xyz.doocode.superbus.core.tts.TtsCountdownManager
import xyz.doocode.superbus.core.tts.TtsSettings

private fun String.normalize(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")

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
    private val referenceDataRepository = ReferenceDataRepository.getInstance(application)

    val ttsManager = TtsCountdownManager(application)

    init {
        ttsManager.init()
    }

    private val _uiState = MutableStateFlow<StopDetailsUiState>(StopDetailsUiState.Loading)
    val uiState: StateFlow<StopDetailsUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    private val _nearbyStops = MutableStateFlow<List<Arret>>(emptyList())
    val nearbyStops: StateFlow<List<Arret>> = _nearbyStops.asStateFlow()

    private val _isLoadingNearbyStops = MutableStateFlow(false)
    val isLoadingNearbyStops: StateFlow<Boolean> = _isLoadingNearbyStops.asStateFlow()

    private val _velociteStation = MutableStateFlow<Station?>(null)
    val velociteStation: StateFlow<Station?> = _velociteStation.asStateFlow()

    private var currentStopName: String? = null
    private var currentStopId: String? = null
    private var detailsFromId: Boolean = true
    private var pollingJob: Job? = null
    private var lastRefreshTime = 0L
    private var nearbyStopsLoaded = false
    private var matchedVelociteStationId: Int? = null

    fun init(stopName: String?, stopId: String?, detailsFromId: Boolean = true) {
        val isNew = (currentStopName != stopName || currentStopId != stopId)
        currentStopName = stopName
        currentStopId = stopId
        this.detailsFromId = detailsFromId

        stopId?.let { id ->
            viewModelScope.launch {
                repository.favorites.collectLatest { favorites ->
                    _isFavorite.value =
                        favorites.any { it.id == id && it.detailsFromId == detailsFromId }
                }
            }
            viewModelScope.launch {
                favoritesManager.refreshFavoriteLines(id, detailsFromId)
            }
        }

        if (isNew) {
            _uiState.value = StopDetailsUiState.Loading
            nearbyStopsLoaded = false
            matchedVelociteStationId = null
            _velociteStation.value = null
            _nearbyStops.value = emptyList()
            loadData(forceRefresh = true)
            // Récupérer détails de la station (getDetailsArret) si l'id est présent
        }
    }

    private val name: String get() = currentStopName ?: ""
    private val id: String get() = currentStopId ?: ""

    fun toggleFavorite() {
        if (id.isEmpty()) return
        viewModelScope.launch {
            favoritesManager.toggleFavorite(id, name, detailsFromId)
        }
    }

    fun refresh() {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < 10000) {
            _isRefreshing.value = false
            return
        }
        lastRefreshTime = now
        loadData(forceRefresh = true)
    }

    fun startAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                if (!_isRefreshing.value) {
                    loadData(forceRefresh = false)
                }
                delay(10000)
            }
        }
    }

    fun stopAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun loadData(forceRefresh: Boolean) {
        if (forceRefresh) _isRefreshing.value = true

        viewModelScope.launch {
            try {
                if (currentStopName != null || currentStopId != null) {
                    val response = when {
                        currentStopId != null && detailsFromId ->
                            ApiClient.ginkoService.getTempsLieu(
                                idArret = currentStopId!!,
                                nb = 5
                            )

                        currentStopName != null && !detailsFromId ->
                            ApiClient.ginkoService.getTempsLieu(nom = currentStopName!!, nb = 5)

                        else -> null
                    }

                    response?.let { res ->
                        val arrivals = res.objects.listeTemps

                        if (arrivals.isEmpty()) {
                            _uiState.value = StopDetailsUiState.Empty
                            if (!nearbyStopsLoaded && currentStopId != null) {
                                loadNearbyStops()
                            }
                        } else {
                            nearbyStopsLoaded = false
                            _nearbyStops.value = emptyList()
                            val grouped = arrivals
                                .groupBy { "${it.numLignePublic}|${it.destination}" }
                                .mapValues { (_, list) ->
                                    list.sortedWith(compareBy { if (it.temps.contains("min")) 0 else 1 })
                                }

                            _uiState.value = StopDetailsUiState.Success(res.objects, grouped)
                            ttsManager.onArrivalsUpdated(grouped)
                        }

                        // Charge (ou rafraîchit) la station Vélocité correspondante
                        loadVelociteStation(res.objects.nomExact)
                    }
                } else {
                    if (forceRefresh) {
                        _uiState.value =
                            StopDetailsUiState.Error("Aucun identifiant de la station est disponible")
                    }
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

    private suspend fun loadVelociteStation(nomExact: String) {
        // Trouve l'ID de la station si pas encore fait
        if (matchedVelociteStationId == null) {
            try {
                val stations = referenceDataRepository.getVelociteStations()
                val normalizedNomExact = nomExact.trim().normalize()
                val matched = stations.firstOrNull { station ->
                    val cleaned = station.name
                        .replaceFirst(Regex("^\\d+\\s*-\\s*"), "")
                        .replace(" (CB)", "")
                        .trim()
                        .normalize()
                    cleaned.equals(normalizedNomExact, ignoreCase = true)
                }
                matchedVelociteStationId = matched?.number
            } catch (_: Exception) {
                return
            }
        }
        val id = matchedVelociteStationId ?: return
        try {
            _velociteStation.value = ApiClient.jcDecauxService.getStation(id)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Garde la dernière valeur connue
        }
    }

    private fun loadNearbyStops() {
        viewModelScope.launch {
            _isLoadingNearbyStops.value = true
            try {
                val stopId = currentStopId ?: return@launch
                val stop = ApiClient.ginkoService.getDetailsArret(stopId).objects
                val rawNearby =
                    ApiClient.ginkoService.getArretsProches(stop.latitude, stop.longitude).objects

                // Group by name exactly like SearchViewModel does
                val grouped = rawNearby
                    .filter { it.id != stopId }
                    .onEach { if (it.duplicates == null) it.duplicates = emptyList() }
                    .groupBy { it.nom }
                    .map { (_, stops) ->
                        val sorted = stops.sortedBy { it.id }
                        sorted.first().copy(
                            duplicates = sorted.onEach {
                                if (it.duplicates == null) it.duplicates = emptyList()
                            }
                        )
                    }
                //.sortedBy { it.nom }

                _nearbyStops.value = grouped
                nearbyStopsLoaded = true
            } catch (_: Exception) {
                // Affichage silencieux — les stations à proximité sont facultatives
            } finally {
                _isLoadingNearbyStops.value = false
            }
        }
    }

    fun toggleTtsSubscription(key: String, numLigne: String, destination: String) {
        ttsManager.toggleSubscription(key, numLigne, destination)
    }

    fun isTtsSubscribed(key: String): Boolean = ttsManager.isSubscribed(key)

    fun hasTtsSubscriptions(): Boolean = ttsManager.hasActiveSubscriptions()

    fun clearTtsSubscriptions() = ttsManager.clearAllSubscriptions()

    fun announceTtsPause() = ttsManager.announcePause()

    fun saveTtsSettings(settings: TtsSettings) = ttsManager.saveSettings(settings)

    fun getTtsSettings(): TtsSettings = ttsManager.getSettings()

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        ttsManager.shutdown()
    }
}
