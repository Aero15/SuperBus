package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.ui.components.EmptyUpcomingPassagesView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.components.StopListItem
import xyz.doocode.superbus.ui.components.StopVariantsBottomSheet
import xyz.doocode.superbus.ui.details.components.ArrivalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailsListContent(
    state: StopDetailsUiState,
    forcedExpandState: Boolean?,
    nearbyStops: List<Arret> = emptyList(),
    isLoadingNearbyStops: Boolean = false,
    onRetry: () -> Unit,
    onItemLongClick: (String) -> Unit,
    onNearbyStopClick: (stop: Arret, fromId: Boolean) -> Unit = { _, _ -> }
) {
    var selectedStop by remember { mutableStateOf<Arret?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(1),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        when (state) {
            is StopDetailsUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingView("Chargement des temps d'attente...")
                    }
                }
            }

            is StopDetailsUiState.Error -> {
                item { ErrorView(state.message, onRetry = onRetry) }
            }

            is StopDetailsUiState.Empty -> {
                item { EmptyUpcomingPassagesView() }
                if (isLoadingNearbyStops) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    }
                }
                if (nearbyStops.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.layout { measurable, constraints ->
                                val offsetPx = 0.dp.roundToPx()
                                val placeable = measurable.measure(
                                    constraints.copy(maxWidth = constraints.maxWidth + 2 * offsetPx)
                                )
                                layout(placeable.width, placeable.height) {
                                    placeable.place(-offsetPx, 0)
                                }
                            }
                        ) {
                            Text(
                                text = "Stations à proximité",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 8.dp,
                                    bottom = 8.dp
                                )
                            )
                            HorizontalDivider()
                            nearbyStops.forEach { stop ->
                                val hasVariants = stop.duplicates.size > 1
                                StopListItem(
                                    stop = stop,
                                    groupDuplicates = hasVariants,
                                    onClick = {
                                        if (hasVariants) onNearbyStopClick(stop, false)
                                        else onNearbyStopClick(stop, true)
                                    },
                                    onVariantsClick = { selectedStop = stop }
                                )
                            }
                        }
                    }
                }
            }

            is StopDetailsUiState.Success -> {
                val list = state.groupedArrivals.toList()
                items(list) { (key, arrivals) ->
                    val parts = key.split("|")
                    ArrivalCard(
                        numLigne = parts.getOrNull(0) ?: "?",
                        destination = parts.getOrNull(1) ?: "?",
                        couleurFond = arrivals.first().couleurFond,
                        couleurTexte = arrivals.first().couleurTexte,
                        ligneId = arrivals.first().idLigne,
                        times = arrivals.take(3),
                        initialExpoMode = list.size < 4,
                        forcedExpandState = forcedExpandState,
                        onLongClick = { onItemLongClick(key) }
                    )
                }
            }
        }
    }

    // Bottom sheet — variantes d'une station à proximité
    if (selectedStop != null) {
        StopVariantsBottomSheet(
            stop = selectedStop!!,
            onDismissRequest = { selectedStop = null },
            onGroupedClick = {
                onNearbyStopClick(selectedStop!!, false)
                selectedStop = null
            },
            onDuplicateClick = { duplicate ->
                onNearbyStopClick(duplicate, true)
                selectedStop = null
            }
        )
    }
}
