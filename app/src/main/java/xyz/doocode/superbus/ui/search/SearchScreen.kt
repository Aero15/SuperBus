package xyz.doocode.superbus.ui.search

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.EmptyResultsView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.components.StopVariantsBottomSheet
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.details.velocite.VelociteDetailsActivity
import xyz.doocode.superbus.ui.search.components.BusStopItem
import xyz.doocode.superbus.ui.search.components.SearchBar
import xyz.doocode.superbus.ui.search.components.SearchFilterOption
import xyz.doocode.superbus.ui.search.components.VelociteStationItem

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    focusOnStart: Boolean = false,
    onFocusConsumed: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val groupDuplicates = viewModel.GROUP_DUPLICATES

    val focusRequester = remember { FocusRequester() }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.refreshStationsCache()
        }
    }

    LaunchedEffect(focusOnStart) {
        if (focusOnStart) {
            focusRequester.requestFocus()
            onFocusConsumed()
        }
    }

    val openStopDetails = { stop: Arret, fromId: Boolean ->
        val intent =
            Intent(context, StopDetailsActivity::class.java).apply {
                putExtra(StopDetailsActivity.EXTRA_STOP_NAME, stop.nom)
                putExtra(StopDetailsActivity.EXTRA_STOP_ID, stop.id)
                putExtra(StopDetailsActivity.EXTRA_DETAILS_FROM_ID, fromId)
            }
        context.startActivity(intent)
    }

    // Bottom Sheet State
    var selectedStop by remember { mutableStateOf<Arret?>(null) }
    var selectedLinkedStation by remember {
        mutableStateOf<xyz.doocode.superbus.core.dto.jcdecaux.Station?>(
            null
        )
    }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Filter state: null = all, true = vélocité only, false = bus/tram only
    var showVeloOnly by remember { mutableStateOf<Boolean?>(null) }

    // Connect to Favorites Repository
    val favoritesRepository = remember { FavoritesRepository.getInstance(context) }
    val favorites by favoritesRepository.favorites.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            selectedFilter =
                when (showVeloOnly) {
                    null -> SearchFilterOption.NONE
                    false -> SearchFilterOption.BUS_TRAMS
                    true -> SearchFilterOption.VELOCITE
                },
            onFilterSelected = { option ->
                showVeloOnly =
                    when (option) {
                        SearchFilterOption.NONE -> null
                        SearchFilterOption.BUS_TRAMS -> false
                        SearchFilterOption.VELOCITE -> true
                    }
            },
            modifier = Modifier.focusRequester(focusRequester)
        )

        when (val state = uiState) {
            is SearchUiState.Loading -> {
                LoadingView()
            }

            is SearchUiState.Error -> {
                ErrorView(message = state.message, onRetry = viewModel::loadData)
            }

            is SearchUiState.Empty -> {
                EmptyDataView()
            }

            is SearchUiState.Success -> {
                if (state.stops.isEmpty() && state.stations.isEmpty()) {
                    EmptyResultsView(query = searchQuery)
                } else {
                    val visibleStops = if (showVeloOnly == true) emptyList() else state.stops
                    val visibleStations = if (showVeloOnly == false) emptyList() else state.stations
                    val totalVisible =
                        if (showVeloOnly == null) state.merged.size
                        else visibleStops.size + visibleStations.size

                    if (totalVisible == 0) {
                        EmptyResultsView(query = searchQuery)
                    } else {
                        Text(
                            text = "$totalVisible résultats",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (showVeloOnly == null) {
                                // Filtre "Tout" : liste fusionnée et triée alphabétiquement
                                items(state.merged) { result ->
                                    when (result) {
                                        is SearchResult.Stop -> {
                                            val stop = result.arret
                                            val favorite =
                                                favorites.find {
                                                    it.id == stop.id &&
                                                            ((stop.duplicates.size > 1 &&
                                                                    !it.detailsFromId) ||
                                                                    (stop.duplicates.size == 1 &&
                                                                            it.detailsFromId))
                                                }
                                            BusStopItem(
                                                stop = stop,
                                                searchQuery = searchQuery,
                                                isFavorite = favorite != null,
                                                favoriteLines = favorite?.lines ?: emptyList(),
                                                groupDuplicates = groupDuplicates,
                                                hasLinkedVelociteStation = result.linkedStation != null,
                                                onFillQuery = { name ->
                                                    viewModel.onSearchQueryChanged(name)
                                                },
                                                onToggleFavorite = {
                                                    viewModel.toggleFavorite(stop)
                                                },
                                                onClick = {
                                                    if (groupDuplicates && stop.duplicates.size > 1) {
                                                        openStopDetails(stop, false)
                                                    } else {
                                                        openStopDetails(stop, groupDuplicates)
                                                    }
                                                },
                                                onVariantsClick = {
                                                    selectedStop = stop
                                                    selectedLinkedStation = result.linkedStation
                                                    showBottomSheet = true
                                                },
                                                onDuplicateClick = { duplicate ->
                                                    openStopDetails(duplicate, true)
                                                }
                                            )
                                        }

                                        is SearchResult.VeloStation -> {
                                            VelociteStationItem(
                                                station = result.station,
                                                searchQuery = searchQuery,
                                                onClick = {
                                                    val intent = Intent(
                                                        context,
                                                        VelociteDetailsActivity::class.java
                                                    ).apply {
                                                        putExtra(
                                                            VelociteDetailsActivity.EXTRA_STATION_ID,
                                                            result.station.number
                                                        )
                                                        putExtra(
                                                            VelociteDetailsActivity.EXTRA_STATION_NAME,
                                                            result.station.name
                                                        )
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Filtres "Bus & Tram" ou "Vélocité" : listes séparées
                                items(visibleStops) { stop ->
                                    val favorite =
                                        favorites.find {
                                            it.id == stop.id &&
                                                    ((stop.duplicates.size > 1 &&
                                                            !it.detailsFromId) ||
                                                            (stop.duplicates.size == 1 &&
                                                                    it.detailsFromId))
                                        }
                                    BusStopItem(
                                        stop = stop,
                                        searchQuery = searchQuery,
                                        isFavorite = favorite != null,
                                        favoriteLines = favorite?.lines ?: emptyList(),
                                        groupDuplicates = groupDuplicates,
                                        hasLinkedVelociteStation =
                                            showVeloOnly != false && state.linkedStationByStopId.containsKey(
                                                stop.id
                                            ),
                                        onFillQuery = { name ->
                                            viewModel.onSearchQueryChanged(name)
                                        },
                                        onToggleFavorite = { viewModel.toggleFavorite(stop) },
                                        onClick = {
                                            if (groupDuplicates && stop.duplicates.size > 1) {
                                                openStopDetails(stop, false)
                                            } else {
                                                openStopDetails(stop, groupDuplicates)
                                            }
                                        },
                                        onVariantsClick = {
                                            selectedStop = stop
                                            selectedLinkedStation =
                                                if (showVeloOnly == false) null
                                                else state.linkedStationByStopId[stop.id]
                                            showBottomSheet = true
                                        },
                                        onDuplicateClick = { duplicate ->
                                            openStopDetails(duplicate, true)
                                        }
                                    )
                                }
                                items(visibleStations) { station ->
                                    VelociteStationItem(
                                        station = station,
                                        searchQuery = searchQuery,
                                        onClick = {
                                            val intent = Intent(
                                                context,
                                                VelociteDetailsActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    VelociteDetailsActivity.EXTRA_STATION_ID,
                                                    station.number
                                                )
                                                putExtra(
                                                    VelociteDetailsActivity.EXTRA_STATION_NAME,
                                                    station.name
                                                )
                                            }
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }
                    } // end else totalVisible > 0

                    if (showBottomSheet && selectedStop != null) {
                        StopVariantsBottomSheet(
                            stop = selectedStop!!,
                            onDismissRequest = {
                                showBottomSheet = false
                                selectedLinkedStation = null
                            },
                            onGroupedClick = {
                                showBottomSheet = false
                                openStopDetails(selectedStop!!, false)
                            },
                            onDuplicateClick = { duplicate ->
                                showBottomSheet = false
                                openStopDetails(duplicate, true)
                            },
                            isGroupedFavorite =
                                favorites.any {
                                    it.id == selectedStop!!.id && !it.detailsFromId
                                },
                            isDuplicateFavorite = { duplicate ->
                                favorites.any { it.id == duplicate.id && it.detailsFromId }
                            },
                            onToggleGroupedFavorite = {
                                viewModel.toggleFavorite(selectedStop!!, false)
                            },
                            onToggleDuplicateFavorite = { duplicate ->
                                viewModel.toggleFavorite(duplicate, true)
                            },
                            onFillQuery = { query ->
                                viewModel.onSearchQueryChanged(query)
                                showBottomSheet = false
                            },
                            velociteStation =
                                if (showVeloOnly == false) null else selectedLinkedStation,
                            onVelociteClick =
                                if (showVeloOnly == false) null
                                else selectedLinkedStation?.let { station ->
                                    {
                                        showBottomSheet = false
                                        val intent =
                                            Intent(
                                                context,
                                                VelociteDetailsActivity::class.java
                                            ).apply {
                                                putExtra(
                                                    VelociteDetailsActivity.EXTRA_STATION_ID,
                                                    station.number
                                                )
                                                putExtra(
                                                    VelociteDetailsActivity.EXTRA_STATION_NAME,
                                                    station.name
                                                )
                                            }
                                        context.startActivity(intent)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
