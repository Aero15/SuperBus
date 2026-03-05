package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.dto.FavoriteStation
import xyz.doocode.superbus.ui.components.SearchBar
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onStationClick: (FavoriteStation) -> Unit,
    onSearchClick: () -> Unit, // Navigate to search to add new (or focus search bar)
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }

    // State for drag and drop
    var draggedItem by remember { mutableStateOf<FavoriteStation?>(null) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var draggedItemSize by remember { mutableStateOf(IntSize.Zero) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isEditing) {
                TopAppBar(
                    title = { Text("Réorganiser") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.cancelEditing() }) {
                            Icon(Icons.Default.Close, contentDescription = "Annuler")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.saveOrder() }) {
                            Icon(Icons.Default.Check, contentDescription = "Enregistrer")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            } else {
                Column {
                    TopAppBar(
                        title = { Text("Favoris", fontWeight = FontWeight.Bold) },
                        actions = {
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "Plus d'options"
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rechercher") },
                                        leadingIcon = { Icon(Icons.Default.Search, null) },
                                        onClick = {
                                            showMenu = false
                                            showSearchBar = !showSearchBar
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Réorganiser") },
                                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                                        onClick = {
                                            showMenu = false
                                            viewModel.startEditing()
                                        }
                                    )
                                }
                            }
                        }
                    )
                    if (showSearchBar) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = viewModel::onSearchQueryChanged,
                            placeholder = "Rechercher",
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {
            if (favorites.isNotEmpty()) {
                Text(
                    text = "${favorites.size} station(s)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (favorites.isEmpty()) {
                if (searchQuery.isNotEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucun favori ne correspond à votre recherche.")
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Aucun favori pour le moment.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onSearchClick) {
                                Text("Trouver une station")
                            }
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    val gridState = rememberLazyGridState()

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(isEditing) {
                                if (isEditing) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { offset ->
                                            val item =
                                                gridState.layoutInfo.visibleItemsInfo.firstOrNull {
                                                    offset.x >= it.offset.x && offset.x <= it.offset.x + it.size.width &&
                                                            offset.y >= it.offset.y && offset.y <= it.offset.y + it.size.height
                                                }
                                            if (item != null) {
                                                val index = item.index
                                                if (index in favorites.indices) {
                                                    draggedItem = favorites[index]
                                                    draggedItemSize = item.size
                                                    touchPosition = offset
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            touchPosition += dragAmount

                                            // Handle reordering
                                            val offset = touchPosition
                                            val itemUnderFinger =
                                                gridState.layoutInfo.visibleItemsInfo.firstOrNull {
                                                    offset.x >= it.offset.x && offset.x <= it.offset.x + it.size.width &&
                                                            offset.y >= it.offset.y && offset.y <= it.offset.y + it.size.height
                                                }

                                            if (itemUnderFinger != null && draggedItem != null) {
                                                val fromIndex =
                                                    favorites.indexOfFirst { it.id == draggedItem!!.id && it.detailsFromId == draggedItem!!.detailsFromId }
                                                val toIndex = itemUnderFinger.index

                                                if (fromIndex != -1 && fromIndex != toIndex) {
                                                    viewModel.moveFavorite(fromIndex, toIndex)
                                                }
                                            }
                                        },
                                        onDragEnd = { draggedItem = null },
                                        onDragCancel = { draggedItem = null }
                                    )
                                }
                            }
                    ) {
                        items(favorites, key = { "${it.id}_${it.detailsFromId}" }) { station ->
                            val isBeingDragged =
                                draggedItem?.id == station.id && draggedItem?.detailsFromId == station.detailsFromId

                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .alpha(if (isBeingDragged) 0f else 1f)
                            ) {
                                FavoriteTile(
                                    station = station,
                                    isEditing = isEditing,
                                    onClick = {
                                        if (!isEditing) onStationClick(station)
                                    }
                                )
                            }
                        }
                    }

                    // Ghost Item (The one under the finger)
                    if (draggedItem != null) {
                        val density = LocalDensity.current
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = (touchPosition.x - draggedItemSize.width / 2).toInt(),
                                        y = (touchPosition.y - draggedItemSize.height / 2).toInt()
                                    )
                                }
                                .size(
                                    width = with(density) { draggedItemSize.width.toDp() },
                                    height = with(density) { draggedItemSize.height.toDp() }
                                )
                                .graphicsLayer {
                                    scaleX = 1.1f
                                    scaleY = 1.1f
                                    alpha = 0.9f
                                    shadowElevation = 8.dp.toPx()
                                }
                        ) {
                            FavoriteTile(
                                station = draggedItem!!,
                                isEditing = true,
                                onClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
