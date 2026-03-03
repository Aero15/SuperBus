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
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

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
                            val favorite = favorites.find { it.id == stop.id }
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
                                        selectedStop = stop
                                        showBottomSheet = true
                                    } else if (!groupDuplicates) {
                                        // Open by Name
                                        val intent = Intent(
                                            context,
                                            StopDetailsActivity::class.java
                                        )
                                        intent.putExtra(
                                            StopDetailsActivity.EXTRA_STOP_NAME,
                                            stop.nom
                                        )
                                        context.startActivity(intent)
                                    } else {
                                        // Open by ID
                                        val intent = Intent(
                                            context,
                                            StopDetailsActivity::class.java
                                        )
                                        intent.putExtra(StopDetailsActivity.EXTRA_STOP_ID, stop.id)
                                        context.startActivity(intent)
                                    }
                                },
                                onDuplicateClick = { duplicate ->
                                    val intent = Intent(
                                        context,
                                        StopDetailsActivity::class.java
                                    )
                                    intent.putExtra(StopDetailsActivity.EXTRA_STOP_ID, duplicate.id)
                                    context.startActivity(intent)
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
                                LazyColumn {
                                    // 1. Recommended Item (Grouped)
                                    item {
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    text = selectedStop!!.nom,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            supportingContent = {
                                                Text("Voir les horaires de tous les quais")
                                            },
                                            leadingContent = {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Rechercher par nom",
                                                    tint = MaterialTheme.colorScheme.primary
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
                                            modifier = Modifier.clickable {
                                                showBottomSheet = false
                                                val intent = Intent(
                                                    context,
                                                    StopDetailsActivity::class.java
                                                )
                                                intent.putExtra(
                                                    StopDetailsActivity.EXTRA_STOP_NAME,
                                                    selectedStop!!.nom
                                                )
                                                context.startActivity(intent)
                                            }
                                        )
                                        HorizontalDivider(thickness = 0.5.dp)
                                    }

                                    // 2. Individual items (By ID)
                                    items(selectedStop!!.duplicates) { duplicate ->
                                        ListItem(
                                            leadingContent = {
                                                Icon(
                                                    imageVector = Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            },
                                            headlineContent = { Text(duplicate.nom) },
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
                                            modifier = Modifier.clickable {
                                                showBottomSheet = false
                                                val intent = Intent(
                                                    context,
                                                    StopDetailsActivity::class.java
                                                )
                                                intent.putExtra(
                                                    StopDetailsActivity.EXTRA_STOP_ID,
                                                    duplicate.id
                                                )
                                                context.startActivity(intent)
                                            }
                                        )
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
