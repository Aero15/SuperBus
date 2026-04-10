package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.ui.components.SearchBar
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.rememberUpdatedState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onStationClick: (FavoriteStation) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isEditing by viewModel.isEditing.collectAsState()
    val selectedIds by viewModel.selectedIds.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()

    BackHandler(enabled = isEditing) { viewModel.cancelEditing() }

    var showMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<FavoriteStation?>(null) }
    var newNameForRename by remember { mutableStateOf("") }

    // State for drag and drop
    var draggedItem by remember { mutableStateOf<FavoriteStation?>(null) }
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var draggedItemSize by remember { mutableStateOf(IntSize.Zero) }

    // Rename dialog — works in both edit mode and normal mode
    if (showRenameDialog != null) {
        val station = showRenameDialog!!
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Renommer le favori") },
            text = {
                OutlinedTextField(
                    value = newNameForRename,
                    onValueChange = { newNameForRename = it },
                    label = { Text("Nom") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isEditing) {
                        viewModel.renameInEditMode(station, newNameForRename)
                    } else {
                        viewModel.renameFavorite(
                            station.id,
                            station.detailsFromId,
                            newNameForRename
                        )
                    }
                    showRenameDialog = null
                }) { Text("Enregistrer") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) { Text("Annuler") }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isEditing) {
                TopAppBar(
                    title = {
                        Text(
                            if (selectedIds.isEmpty()) "Réorganiser"
                            else "${selectedIds.size} sélectionné(s)"
                        )
                    },
                    windowInsets = WindowInsets(0),
                    navigationIcon = {
                        IconButton(onClick = { viewModel.cancelEditing() }) {
                            Icon(Icons.Default.Close, contentDescription = "Annuler")
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.undo() }, enabled = canUndo) {
                            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Annuler")
                        }
                        IconButton(onClick = { viewModel.redo() }, enabled = canRedo) {
                            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Rétablir")
                        }
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
                        windowInsets = WindowInsets(0),
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (isEditing && selectedIds.isNotEmpty()) {
                BottomAppBar(
                    windowInsets = WindowInsets(0),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    IconButton(
                        onClick = { viewModel.invertSelection() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Text("Inverser", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.selectAll() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SelectAll, contentDescription = null)
                            Text("Tout", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    IconButton(
                        onClick = { viewModel.deleteSelected() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                "Supprimer",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
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
                    text = "${favorites.size} favori(s)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
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
                    var gridHeightPx by remember { mutableStateOf(0) }
                    val currentSelectedIds by rememberUpdatedState(selectedIds)

                    LaunchedEffect(draggedItem != null) {
                        while (draggedItem != null) {
                            val threshold = 160f
                            val speed = 22f
                            val y = touchPosition.y
                            val delta = when {
                                y in 0f..threshold -> -(1f - y / threshold) * speed
                                gridHeightPx > 0 && y > gridHeightPx - threshold ->
                                    (1f - (gridHeightPx - y) / threshold) * speed

                                else -> 0f
                            }
                            if (delta != 0f) gridState.scrollBy(delta)
                            delay(16L)
                        }
                    }

                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { gridHeightPx = it.height }
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
                                                    viewModel.captureUndoBeforeDrag()
                                                    draggedItem = favorites[index]
                                                    draggedItemSize = item.size
                                                    touchPosition = offset
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            touchPosition += dragAmount
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
                                                    val draggedKey =
                                                        "${draggedItem?.id}_${draggedItem?.detailsFromId}"
                                                    if (draggedKey in currentSelectedIds && currentSelectedIds.size > 1) {
                                                        viewModel.moveSelectedFavorites(
                                                            draggedItem!!,
                                                            toIndex
                                                        )
                                                    } else {
                                                        viewModel.moveFavorite(fromIndex, toIndex)
                                                    }
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
                            val stationKey = "${station.id}_${station.detailsFromId}"
                            val isBeingDragged =
                                draggedItem?.id == station.id && draggedItem?.detailsFromId == station.detailsFromId
                            val isSelected = stationKey in selectedIds
                            val isSingleSelection = selectedIds.size == 1

                            Box(
                                modifier = Modifier
                                    .animateItem()
                                    .alpha(if (isBeingDragged) 0f else 1f)
                            ) {
                                FavoriteTile(
                                    station = station,
                                    isEditing = isEditing,
                                    isSelected = isSelected,
                                    isSingleSelection = isSingleSelection && isSelected,
                                    onClick = {
                                        if (isEditing) {
                                            viewModel.toggleSelection(station)
                                        } else {
                                            onStationClick(station)
                                        }
                                    },
                                    onLongPress = {
                                        if (!isEditing) {
                                            viewModel.startEditingWithSelection(station)
                                        }
                                    },
                                    onRename = {
                                        newNameForRename = station.name
                                        showRenameDialog = station
                                    },
                                    onRemove = {
                                        if (isEditing) {
                                            viewModel.toggleSelection(station)
                                            viewModel.deleteSelected()
                                        } else {
                                            viewModel.removeFavorite(
                                                station.id,
                                                station.detailsFromId
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Ghost Item
                    if (draggedItem != null) {
                        val density = LocalDensity.current
                        val draggedKey = "${draggedItem!!.id}_${draggedItem!!.detailsFromId}"
                        val extraCount =
                            if (draggedKey in selectedIds && selectedIds.size > 1) selectedIds.size - 1 else 0
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
                            if (extraCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$extraCount",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
