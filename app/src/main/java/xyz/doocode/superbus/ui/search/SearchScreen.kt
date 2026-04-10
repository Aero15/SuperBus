package xyz.doocode.superbus.ui.search

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.ui.components.*
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.EmptyResultsView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.components.SearchBar
import xyz.doocode.superbus.ui.components.StopListItem
import xyz.doocode.superbus.ui.details.StopDetailsActivity

@OptIn(ExperimentalMaterial3Api::class)
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
            modifier = Modifier.focusRequester(focusRequester)
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .selectableGroup(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chipSize = 48.dp
            val iconSize = 24.dp

            // Tout (*)
            FilterChip(
                selected = showVeloOnly == null,
                onClick = { showVeloOnly = null },
                label = {
                    Text(text = "✱", fontSize = 20.sp, modifier = Modifier.size(iconSize))
                },
                shape = CircleShape,
                modifier = Modifier.size(chipSize),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Bus & Tram
            FilterChip(
                selected = showVeloOnly == false,
                onClick = { showVeloOnly = false },
                label = {
                    Icon(
                        Icons.Default.DirectionsBus,
                        contentDescription = "Bus & Tram",
                        modifier = Modifier.size(iconSize)
                    )
                },
                shape = CircleShape,
                modifier = Modifier.size(chipSize),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Vélocité
            FilterChip(
                selected = showVeloOnly == true,
                onClick = { showVeloOnly = true },
                label = {
                    Icon(
                        Icons.AutoMirrored.Filled.DirectionsBike,
                        contentDescription = "Vélocité",
                        modifier = Modifier.size(iconSize)
                    )
                },
                shape = CircleShape,
                modifier = Modifier.size(chipSize),
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor =
                            MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
            )
        }

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
                                                                    (stop.duplicates.size ==
                                                                            1 &&
                                                                            it.detailsFromId))
                                                }
                                            StopListItem(
                                                stop = stop,
                                                searchQuery = searchQuery,
                                                isFavorite = favorite != null,
                                                favoriteLines = favorite?.lines ?: emptyList(),
                                                groupDuplicates =
                                                    groupDuplicates &&
                                                            stop.duplicates.size > 1,
                                                onFillQuery = { name ->
                                                    viewModel.onSearchQueryChanged(name)
                                                },
                                                onToggleFavorite = {
                                                    viewModel.toggleFavorite(stop)
                                                },
                                                onClick = {
                                                    if (groupDuplicates &&
                                                        stop.duplicates.size > 1
                                                    ) {
                                                        openStopDetails(stop, false)
                                                    } else {
                                                        openStopDetails(stop, groupDuplicates)
                                                    }
                                                },
                                                onVariantsClick = {
                                                    selectedStop = stop
                                                    showBottomSheet = true
                                                },
                                                onDuplicateClick = { duplicate ->
                                                    openStopDetails(duplicate, true)
                                                }
                                            )
                                        }

                                        is SearchResult.VeloStation -> {
                                            StationListItem(
                                                station = result.station,
                                                searchQuery = searchQuery,
                                                onClick = {
                                                    val intent =
                                                        Intent(
                                                            context,
                                                            xyz.doocode.superbus
                                                                .ui
                                                                .details
                                                                .velocite
                                                                .VelociteDetailsActivity::class
                                                                .java
                                                        )
                                                            .apply {
                                                                putExtra(
                                                                    xyz.doocode
                                                                        .superbus
                                                                        .ui
                                                                        .details
                                                                        .velocite
                                                                        .VelociteDetailsActivity
                                                                        .EXTRA_STATION_ID,
                                                                    result.station
                                                                        .number
                                                                )
                                                                putExtra(
                                                                    xyz.doocode
                                                                        .superbus
                                                                        .ui
                                                                        .details
                                                                        .velocite
                                                                        .VelociteDetailsActivity
                                                                        .EXTRA_STATION_NAME,
                                                                    result.station
                                                                        .name
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
                                    StopListItem(
                                        stop = stop,
                                        searchQuery = searchQuery,
                                        isFavorite = favorite != null,
                                        favoriteLines = favorite?.lines ?: emptyList(),
                                        groupDuplicates =
                                            groupDuplicates && stop.duplicates.size > 1,
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
                                            showBottomSheet = true
                                        },
                                        onDuplicateClick = { duplicate ->
                                            openStopDetails(duplicate, true)
                                        }
                                    )
                                }
                                items(visibleStations) { station ->
                                    StationListItem(
                                        station = station,
                                        searchQuery = searchQuery,
                                        onClick = {
                                            val intent =
                                                Intent(
                                                    context,
                                                    xyz.doocode.superbus.ui
                                                        .details
                                                        .velocite
                                                        .VelociteDetailsActivity::class
                                                        .java
                                                )
                                                    .apply {
                                                        putExtra(
                                                            xyz.doocode.superbus.ui
                                                                .details
                                                                .velocite
                                                                .VelociteDetailsActivity
                                                                .EXTRA_STATION_ID,
                                                            station.number
                                                        )
                                                        putExtra(
                                                            xyz.doocode.superbus.ui
                                                                .details
                                                                .velocite
                                                                .VelociteDetailsActivity
                                                                .EXTRA_STATION_NAME,
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
                            onDismissRequest = { showBottomSheet = false },
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
                            }
                        )
                    }
                }
            }
        }
    }
}
