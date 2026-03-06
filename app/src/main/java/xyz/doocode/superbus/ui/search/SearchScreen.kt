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
import xyz.doocode.superbus.core.dto.Arret
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val groupDuplicates = viewModel.GROUP_DUPLICATES

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
    val sheetState = rememberModalBottomSheetState()

    // Connect to Favorites Repository
    val favoritesRepository = remember { FavoritesRepository.getInstance(context) }
    val favorites by favoritesRepository.favorites.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        SearchBar(
            query = searchQuery,
            onQueryChange = viewModel::onSearchQueryChanged
        )

        when (val state = uiState) {
            is SearchUiState.Loading -> {
                LoadingView()
            }

            is SearchUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = viewModel::loadStops
                )
            }

            is SearchUiState.Empty -> {
                EmptyDataView()
            }

            is SearchUiState.Success -> {
                if (state.stops.isEmpty()) {
                    EmptyResultsView(query = searchQuery)
                } else {
                    Text(
                        text = "${state.stops.size} arrêts trouvés",
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
                    }

                    if (showBottomSheet && selectedStop != null) {
                        ModalBottomSheet(
                            onDismissRequest = { showBottomSheet = false },
                            sheetState = sheetState
                        ) {
                            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                                Text(
                                    text = "${selectedStop?.nom}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                                HorizontalDivider()
                                LazyColumn(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                                    // 1. Recommended Item (Grouped)
                                    item {
                                        var showMenu by remember { mutableStateOf(false) }

                                        val isFav =
                                            favorites.any { it.id == selectedStop!!.id && !it.detailsFromId }

                                        Box {
                                            ListItem(
                                                headlineContent = {
                                                    val parts =
                                                        selectedStop!!.nom.split(" - ", limit = 2)
                                                    if (parts.size == 2) {
                                                        Column {
                                                            Text(
                                                                text = parts[0],
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                                    alpha = 0.8f
                                                                )
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
                                                            text = selectedStop!!.nom,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                },
                                                supportingContent = {
                                                    Text("Voir les horaires de tous les quais")
                                                },
                                                leadingContent = {
                                                    val icon =
                                                        if (isFav) Icons.Default.Favorite else Icons.Default.Search
                                                    val tint =
                                                        if (isFav) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = if (isFav) "Favori" else "Rechercher par nom",
                                                        tint = tint
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
                                                colors = ListItemDefaults.colors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                                        alpha = 0.3f
                                                    )
                                                ),
                                                modifier = Modifier
                                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                                    .clip(MaterialTheme.shapes.medium)
                                                    .pointerInput(Unit) {
                                                        detectTapGestures(
                                                            onTap = {
                                                                showBottomSheet = false
                                                                openStopDetails(
                                                                    selectedStop!!,
                                                                    false
                                                                )
                                                            },
                                                            onLongPress = { showMenu = true }
                                                        )
                                                    }
                                            )

                                            StopActionsContainer(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false },
                                                stopName = selectedStop!!.nom,
                                                stopId = selectedStop!!.id,
                                                isFavorite = isFav,
                                                onToggleFavorite = {
                                                    viewModel.toggleFavorite(
                                                        selectedStop!!,
                                                        false
                                                    )
                                                },
                                                onFillQuery = { query ->
                                                    viewModel.onSearchQueryChanged(query)
                                                    showBottomSheet = false
                                                }
                                            )
                                        }
                                        HorizontalDivider(thickness = 0.5.dp)
                                    }

                                    // 2. Individual items (By ID)
                                    items(selectedStop!!.duplicates) { duplicate ->
                                        var showMenu by remember { mutableStateOf(false) }

                                        val isTram = duplicate.id.startsWith("t_")
                                        val isFav =
                                            favorites.any { it.id == duplicate.id && it.detailsFromId }

                                        val icon =
                                            if (isFav) Icons.Default.Favorite else if (isTram) Icons.Default.Tram else Icons.Default.DirectionsBus
                                        val iconColor =
                                            if (isFav) Color(0xFFE91E63) else if (isTram) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary

                                        Box {
                                            ListItem(
                                                leadingContent = {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = if (isFav) "Favori" else if (isTram) "Arrêt de tram" else "Arrêt de bus",
                                                        tint = iconColor
                                                    )
                                                },
                                                headlineContent = {
                                                    val parts =
                                                        duplicate.nom.split(" - ", limit = 2)
                                                    if (parts.size == 2) {
                                                        Column {
                                                            Text(
                                                                text = parts[0],
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                                    alpha = 0.8f
                                                                )
                                                            )
                                                            Text(
                                                                text = parts[1],
                                                                style = MaterialTheme.typography.bodyLarge
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = duplicate.nom,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                },
                                                trailingContent = {
                                                    Text(
                                                        text = "#${duplicate.id}",
                                                        fontFamily = FontFamily.Monospace,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                            alpha = 0.6f
                                                        ),
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                },
                                                modifier = Modifier.pointerInput(Unit) {
                                                    detectTapGestures(
                                                        onTap = {
                                                            showBottomSheet = false
                                                            openStopDetails(duplicate, true)
                                                        },
                                                        onLongPress = { showMenu = true }
                                                    )
                                                }
                                            )

                                            StopActionsContainer(
                                                expanded = showMenu,
                                                onDismissRequest = { showMenu = false },
                                                stopName = duplicate.nom,
                                                stopId = duplicate.id,
                                                isFavorite = isFav,
                                                onToggleFavorite = {
                                                    viewModel.toggleFavorite(
                                                        duplicate,
                                                        true
                                                    )
                                                },
                                                onFillQuery = { query ->
                                                    viewModel.onSearchQueryChanged(query)
                                                    showBottomSheet = false
                                                }
                                            )
                                        }
                                        HorizontalDivider(thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
