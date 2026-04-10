package xyz.doocode.superbus.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.ui.components.EmptyUpcomingPassagesView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.StopListItem
import xyz.doocode.superbus.ui.components.StopVariantsBottomSheet
import xyz.doocode.superbus.ui.details.components.ArrivalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailsListContent(
    state: StopDetailsUiState,
    forcedExpandState: Boolean?,
    forcedSectionsExpandState: Boolean? = null,
    nearbyStops: List<Arret> = emptyList(),
    isLoadingNearbyStops: Boolean = false,
    onRetry: () -> Unit,
    onItemLongClick: (String) -> Unit,
    onNearbyStopClick: (stop: Arret, fromId: Boolean) -> Unit = { _, _ -> }
) {
    var selectedStop by remember { mutableStateOf<Arret?>(null) }
    val expandedSections = remember { mutableStateMapOf(0 to true, 1 to true, 2 to true) }

    LaunchedEffect(forcedSectionsExpandState) {
        if (forcedSectionsExpandState != null) {
            expandedSections[0] = forcedSectionsExpandState
            expandedSections[1] = forcedSectionsExpandState
            expandedSections[2] = forcedSectionsExpandState
        }
    }

    when (state) {
        is StopDetailsUiState.Loading -> {
            StopDetailsLoadingView()
        }

        is StopDetailsUiState.Error -> {
            ErrorView(state.message, onRetry = onRetry)
        }

        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (state) {
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
                        val lianeRegex = Regex("^L\\d+$")

                        val tramEntries =
                            list.filter { (_, arrivals) -> arrivals.first().modeTransport == 1 }
                        val busEntries =
                            list.filter { (_, arrivals) -> arrivals.first().modeTransport == 0 }
                        val lianeEntries =
                            busEntries.filter { (_, arrivals) ->
                                arrivals.first().numLignePublic.matches(lianeRegex)
                            }
                        val regularBusEntries =
                            busEntries.filter { (_, arrivals) ->
                                !arrivals.first().numLignePublic.matches(lianeRegex)
                            }

                        // Ordre : Tram (1) → Lianes (2) → Bus (0)
                        // TODO: Periurbain, Scolaire, etc. ? (actuellement regroupés avec les bus classiques)
                        val sections = listOf(
                            Triple(1, tramEntries, "tram"),
                            Triple(2, lianeEntries, "lianes"),
                            Triple(0, regularBusEntries, "bus")
                        ).filter { (_, entries, _) -> entries.isNotEmpty() }

                        val hasMixedSections = sections.size > 1

                        sections.forEach { (sectionKey, sectionEntries, _) ->
                            val isExpanded = expandedSections[sectionKey] != false

                            if (hasMixedSections) {
                                item(key = "header_$sectionKey") {
                                    TransportSectionHeader(
                                        mode = sectionKey,
                                        isExpanded = isExpanded,
                                        onToggle = {
                                            expandedSections[sectionKey] = !isExpanded
                                        }
                                    )
                                }
                            }

                            item(key = "section_$sectionKey") {
                                AnimatedVisibility(
                                    visible = !hasMixedSections || isExpanded,
                                    enter = expandVertically(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    ),
                                    exit = shrinkVertically(
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        sectionEntries.forEach { (key, arrivals) ->
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
                        }
                    }

                    else -> {}
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

@Composable
private fun TransportSectionHeader(mode: Int, isExpanded: Boolean, onToggle: () -> Unit) {
    val label = when (mode) {
        0 -> "Bus"
        1 -> "Tram"
        2 -> "Lianes"
        else -> "Autre"
    }
    val icon = when (mode) {
        1 -> Icons.Filled.Tram
        else -> Icons.Filled.DirectionsBus
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) "Réduire" else "Développer"
        )
    }
}

@Composable
fun StopDetailsLoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingTransition")
    val subtitleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "subtitleAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Chargement en cours",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Récupération des informations de cette station",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = subtitleAlpha),
                textAlign = TextAlign.Center
            )
        }
    }
}
