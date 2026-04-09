package xyz.doocode.superbus.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.EmptyResultsView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.components.SearchBar
import xyz.doocode.superbus.ui.components.StopListItem

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.data.FavoritesRepository
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.components.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.ui.components.StopActionsContainer
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

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
        val intent = Intent(context, StopDetailsActivity::class.java).apply {
            putExtra(StopDetailsActivity.EXTRA_STOP_NAME, stop.nom)
            putExtra(StopDetailsActivity.EXTRA_STOP_ID, stop.id)
            putExtra(StopDetailsActivity.EXTRA_DETAILS_FROM_ID, fromId)
        }
        context.startActivity(intent)
    }

    // Bottom Sheet State
    var selectedStop by remember { mutableStateOf<Arret?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Connect to Favorites Repository
    val favoritesRepository = remember { FavoritesRepository.getInstance(context) }
    val favorites by favoritesRepository.favorites.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged,
            modifier = Modifier.focusRequester(focusRequester)
        )

        when (val state = uiState) {
            is SearchUiState.Loading -> {
                LoadingView()
            }

            is SearchUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = viewModel::loadData
                )
            }

            is SearchUiState.Empty -> {
                EmptyDataView()
            }

            is SearchUiState.Success -> {
                if (state.stops.isEmpty() && state.stations.isEmpty()) {
                    EmptyResultsView(query = searchQuery)
                } else {
                    Text(
                        text = "${state.stops.size + state.stations.size} résultats",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.stops) { stop ->
                            val favorite =
                                favorites.find {
                                    it.id == stop.id && (
                                            (stop.duplicates.size > 1 && !it.detailsFromId) ||
                                                    (stop.duplicates.size == 1 && it.detailsFromId)
                                            )
                                }
                            val isFavorite = favorite != null

                            StopListItem(
                                stop = stop,
                                searchQuery = searchQuery,
                                isFavorite = isFavorite,
                                favoriteLines = favorite?.lines ?: emptyList(),
                                groupDuplicates = groupDuplicates && stop.duplicates.size > 1,
                                onFillQuery = { name -> viewModel.onSearchQueryChanged(name) },
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
                                    showBottomSheet = true
                                },
                                onDuplicateClick = { duplicate ->
                                    openStopDetails(duplicate, true)
                                }
                            )
                        }

                        items(state.stations) { station ->
                            StationListItem(
                                station = station,
                                searchQuery = searchQuery,
                                onClick = {
                                    // TODO: Implement display of Velocite station
                                }
                            )
                        }
                    }

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
                            isGroupedFavorite = favorites.any { it.id == selectedStop!!.id && !it.detailsFromId },
                            isDuplicateFavorite = { duplicate ->
                                favorites.any { it.id == duplicate.id && it.detailsFromId }
                            },
                            onToggleGroupedFavorite = {
                                viewModel.toggleFavorite(
                                    selectedStop!!,
                                    false
                                )
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
