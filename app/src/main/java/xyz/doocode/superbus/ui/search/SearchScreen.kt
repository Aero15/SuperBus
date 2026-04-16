package xyz.doocode.superbus.ui.search

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.EmptyResultsView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.components.StopActionsContainer
import xyz.doocode.superbus.ui.components.StopVariantsBottomSheet
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.details.velocite.VelociteDetailsActivity
import xyz.doocode.superbus.ui.search.components.BusStopItem
import xyz.doocode.superbus.ui.search.components.SearchBar
import xyz.doocode.superbus.ui.search.components.SearchFilterOption
import xyz.doocode.superbus.ui.search.components.VelociteStationItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    focusOnStart: Boolean = false,
    onFocusConsumed: () -> Unit = {},
    initialFilter: SearchFilterOption = SearchFilterOption.NONE,
    onFilterConsumed: () -> Unit = {}
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
        mutableStateOf<Station?>(
            null
        )
    }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Filter state: null = all, true = vélocité only, false = bus/tram only
    var showVeloOnly by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(initialFilter) {
        if (initialFilter == SearchFilterOption.VELOCITE) {
            showVeloOnly = true
            onFilterConsumed()
        }
    }

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

                    val inlineGroupedResult: Pair<Arret, Station?>? =
                        when (showVeloOnly) {
                            null -> {
                                val singleResult = state.merged.singleOrNull() as? SearchResult.Stop
                                singleResult
                                    ?.takeIf {
                                        groupDuplicates &&
                                                (it.arret.duplicates.size > 1 || it.linkedStation != null)
                                    }
                                    ?.let { it.arret to it.linkedStation }
                            }

                            false -> {
                                val singleStop = visibleStops.singleOrNull()
                                singleStop
                                    ?.takeIf {
                                        groupDuplicates && it.duplicates.size > 1
                                    }
                                    ?.let { it to null }
                            }

                            true -> null
                        }

                    val inlineGroupedDisplayCount =
                        inlineGroupedResult?.let { (groupedStop, linkedStation) ->
                            1 + groupedStop.duplicates.size + if (linkedStation != null && showVeloOnly != false) 1 else 0
                        }
                    val displayedCount = inlineGroupedDisplayCount ?: totalVisible

                    if (totalVisible == 0) {
                        EmptyResultsView(query = searchQuery)
                    } else {
                        Text(
                            text = "$displayedCount résultats",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            if (inlineGroupedResult != null) {
                                val groupedStop = inlineGroupedResult.first
                                val linkedStation = inlineGroupedResult.second

                                item("recommended_stop") {
                                    val groupedFavorite =
                                        favorites.any { it.id == groupedStop.id && !it.detailsFromId }
                                    var showMenu by remember { mutableStateOf(false) }

                                    val groupedIcon =
                                        if (groupedFavorite) Icons.Default.Favorite else Icons.Default.Search
                                    val groupedIconTint =
                                        if (groupedFavorite) Color(0xFFE91E63)
                                        else MaterialTheme.colorScheme.primary

                                    Box {
                                        ListItem(
                                            headlineContent = {
                                                val parts = groupedStop.nom.split(" - ", limit = 2)
                                                if (parts.size == 2) {
                                                    Column {
                                                        Text(
                                                            text = parts[0],
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color =
                                                                MaterialTheme.colorScheme
                                                                    .onSurfaceVariant.copy(alpha = 0.8f)
                                                        )
                                                        Text(
                                                            text = parts[1],
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        text = groupedStop.nom,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },
                                            supportingContent = {
                                                Text("Voir les horaires de tous les quais")
                                            },
                                            leadingContent = {
                                                Icon(
                                                    imageVector = groupedIcon,
                                                    contentDescription = null,
                                                    tint = groupedIconTint
                                                )
                                            },
                                            trailingContent = {
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = MaterialTheme.shapes.extraSmall
                                                ) {
                                                    Text(
                                                        text = "Recommandé",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        modifier = Modifier.padding(
                                                            horizontal = 6.dp,
                                                            vertical = 2.dp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                }
                                            },
                                            colors =
                                                ListItemDefaults.colors(
                                                    containerColor =
                                                        MaterialTheme.colorScheme.primaryContainer
                                                            .copy(alpha = 0.3f)
                                                ),
                                            modifier =
                                                Modifier
                                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .combinedClickable(
                                                        onClick = {
                                                            openStopDetails(groupedStop, false)
                                                        },
                                                        onLongClick = { showMenu = true }
                                                    )
                                        )

                                        StopActionsContainer(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            stopName = groupedStop.nom,
                                            stopId = groupedStop.id,
                                            isFavorite = groupedFavorite,
                                            onToggleFavorite = {
                                                viewModel.toggleFavorite(groupedStop, false)
                                            },
                                            onFillQuery = { query ->
                                                viewModel.onSearchQueryChanged(query)
                                            }
                                        )
                                    }

                                    HorizontalDivider(thickness = 0.5.dp)
                                }

                                items(groupedStop.duplicates) { duplicate ->
                                    var showMenu by remember { mutableStateOf(false) }
                                    val isFav =
                                        favorites.any { it.id == duplicate.id && it.detailsFromId }
                                    val isTram = duplicate.id.startsWith("t_")
                                    val multipleTrams =
                                        groupedStop.duplicates.count { it.id.startsWith("t_") } > 1
                                    val multipleBuses =
                                        groupedStop.duplicates.count { !it.id.startsWith("t_") } > 1
                                    val idNumber = duplicate.id.filter { it.isDigit() }

                                    val dupIcon =
                                        when {
                                            isFav -> Icons.Default.Favorite
                                            isTram -> Icons.Default.Tram
                                            else -> Icons.Default.DirectionsBus
                                        }
                                    val dupIconTint =
                                        when {
                                            isFav -> Color(0xFFE91E63)
                                            isTram -> Color(0xFF4CAF50)
                                            else -> Color(0xFFFF6D00)
                                        }
                                    val dupLabel =
                                        when {
                                            isTram && multipleTrams && idNumber.isNotEmpty() ->
                                                "Quai de tramway n°$idNumber"

                                            isTram -> "Quai de tramway"
                                            multipleBuses && idNumber.isNotEmpty() -> "Arrêt de bus n°$idNumber"
                                            else -> "Arrêt de bus"
                                        }

                                    Box {
                                        ListItem(
                                            leadingContent = {
                                                Icon(
                                                    imageVector = dupIcon,
                                                    contentDescription = null,
                                                    tint = dupIconTint
                                                )
                                            },
                                            headlineContent = {
                                                Text(
                                                    text = dupLabel,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            trailingContent = {
                                                Text(
                                                    text = "#${duplicate.id}",
                                                    fontFamily = FontFamily.Monospace,
                                                    color =
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.6f
                                                        ),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            modifier =
                                                Modifier.combinedClickable(
                                                    onClick = {
                                                        openStopDetails(duplicate, true)
                                                    },
                                                    onLongClick = { showMenu = true }
                                                )
                                        )

                                        StopActionsContainer(
                                            expanded = showMenu,
                                            onDismissRequest = { showMenu = false },
                                            stopName = duplicate.nom,
                                            stopId = duplicate.id,
                                            isFavorite = isFav,
                                            onToggleFavorite = {
                                                viewModel.toggleFavorite(duplicate, true)
                                            },
                                            onFillQuery = { query ->
                                                viewModel.onSearchQueryChanged(query)
                                            }
                                        )
                                    }

                                    HorizontalDivider(thickness = 0.5.dp)
                                }

                                if (linkedStation != null && showVeloOnly != false) {
                                    item("linked_velocite_station") {
                                        ListItem(
                                            leadingContent = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                                                    contentDescription = null,
                                                    tint = Color(0xFF00AAC2)
                                                )
                                            },
                                            headlineContent = {
                                                Text(
                                                    text = "Vélocité",
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            },
                                            trailingContent = {
                                                Text(
                                                    text = "#${linkedStation.number}",
                                                    fontFamily = FontFamily.Monospace,
                                                    color =
                                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.6f
                                                        ),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            modifier = Modifier.clickable {
                                                val intent = Intent(
                                                    context,
                                                    VelociteDetailsActivity::class.java
                                                ).apply {
                                                    putExtra(
                                                        VelociteDetailsActivity.EXTRA_STATION_ID,
                                                        linkedStation.number
                                                    )
                                                    putExtra(
                                                        VelociteDetailsActivity.EXTRA_STATION_NAME,
                                                        linkedStation.name
                                                    )
                                                }
                                                context.startActivity(intent)
                                            }
                                        )
                                        HorizontalDivider(thickness = 0.5.dp)
                                    }
                                }
                            } else if (showVeloOnly == null) {
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
