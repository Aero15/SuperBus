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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBike
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
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.ui.components.EmptyUpcomingPassagesView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.StopVariantsBottomSheet
import xyz.doocode.superbus.ui.details.components.ArrivalCard
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.details.velocite.components.VelociteDetailsContent
import xyz.doocode.superbus.ui.details.velocite.components.VelociteRecap
import xyz.doocode.superbus.ui.search.components.BusStopItem

enum class GroupingMode {
    BY_TRANSPORT,
    BY_DIRECTION,
    NONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailsListContent(
    state: StopDetailsUiState,
    selectedTab: StopDetailsTab = StopDetailsTab.SCHEDULES,
    forcedExpandState: Boolean?,
    forcedSectionsExpandState: Boolean? = null,
    groupingMode: GroupingMode = GroupingMode.BY_TRANSPORT,
    velociteStation: Station? = null,
    onVelociteClick: (() -> Unit)? = null,
    nearbyStops: List<Arret> = emptyList(),
    favorites: List<FavoriteStation> = emptyList(),
    isLoadingNearbyStops: Boolean = false,
    onRetry: () -> Unit,
    onItemLongClick: (String) -> Unit,
    onArrivalTimeClick: (key: String, timeIndex: Int) -> Unit = { _, _ -> },
    onNearbyStopClick: (stop: Arret, fromId: Boolean) -> Unit = { _, _ -> },
    onToggleNearbyFavorite: (stop: Arret, fromId: Boolean) -> Unit = { _, _ -> },
    onFillQuery: (String) -> Unit = {}
) {
    var selectedStop by remember { mutableStateOf<Arret?>(null) }

    fun isNearbyStopFavorite(stop: Arret): Boolean {
        val duplicateIds = stop.duplicates.ifEmpty { listOf(stop) }.map { it.id }.toSet()
        return favorites.any { favorite ->
            favorite.effectiveKind == FavoriteStation.KIND_BUS_TRAM && (
                    (!favorite.detailsFromId && favorite.id == stop.id) ||
                            (favorite.detailsFromId && favorite.id in duplicateIds)
                    )
        }
    }

    fun isNearbyDuplicateFavorite(stop: Arret): Boolean {
        return favorites.any { favorite ->
            favorite.effectiveKind == FavoriteStation.KIND_BUS_TRAM &&
                    favorite.detailsFromId && favorite.id == stop.id
        }
    }

    val expandedSections =
        remember {
            mutableStateMapOf(
                0 to true,
                1 to true,
                2 to true,
                3 to true,
                4 to true,
                5 to true,
                10 to true,
                11 to true
            )
        }

    LaunchedEffect(forcedSectionsExpandState) {
        if (forcedSectionsExpandState != null) {
            expandedSections[0] = forcedSectionsExpandState
            expandedSections[1] = forcedSectionsExpandState
            expandedSections[2] = forcedSectionsExpandState
            expandedSections[3] = forcedSectionsExpandState
            expandedSections[4] = forcedSectionsExpandState
            expandedSections[5] = forcedSectionsExpandState
            expandedSections[10] = forcedSectionsExpandState
            expandedSections[11] = forcedSectionsExpandState
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
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (selectedTab) {
                    StopDetailsTab.SCHEDULES -> {
                        when (state) {
                            is StopDetailsUiState.Empty -> {
                                item { EmptyUpcomingPassagesView() }
                            }

                            is StopDetailsUiState.Success -> {
                                val list = state.groupedArrivals.toList()

                                when (groupingMode) {
                                    GroupingMode.BY_TRANSPORT -> {
                                        val lianeRegex = Regex("^L\\d+$")
                                        val scolaireRegex = Regex("^D([1-9]\\d{0,2})$")

                                        val tramEntries =
                                            list.filter { (_, arrivals) -> arrivals.first().modeTransport == 1 }
                                        val busEntries =
                                            list.filter { (_, arrivals) -> arrivals.first().modeTransport == 0 }

                                        val sections = listOf(
                                            Triple(1, tramEntries, "tram"),
                                            Triple(
                                                2,
                                                busEntries.filter { (_, arrivals) ->
                                                    arrivals.first().numLignePublic.matches(
                                                        lianeRegex
                                                    )
                                                },
                                                "lianes"
                                            ),
                                            Triple(
                                                0,
                                                busEntries.filter { (_, arrivals) ->
                                                    val first = arrivals.first()
                                                    !first.numLignePublic.matches(lianeRegex) &&
                                                            !first.numLignePublic.matches(
                                                                scolaireRegex
                                                            ) &&
                                                            !((first.idLigne.toIntOrNull()
                                                                ?: 0) in 50..99)
                                                },
                                                "bus"
                                            ),
                                            Triple(
                                                4,
                                                busEntries.filter { (_, arrivals) ->
                                                    val first = arrivals.first()
                                                    !first.numLignePublic.matches(lianeRegex) &&
                                                            !first.numLignePublic.matches(
                                                                scolaireRegex
                                                            ) &&
                                                            ((first.idLigne.toIntOrNull()
                                                                ?: 0) in 50..99)
                                                },
                                                "periurbain"
                                            ),
                                            Triple(
                                                5,
                                                busEntries.filter { (_, arrivals) ->
                                                    arrivals.first().numLignePublic.matches(
                                                        scolaireRegex
                                                    )
                                                },
                                                "scolaire"
                                            )
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
                                                            expandedSections[sectionKey] =
                                                                !isExpanded
                                                        }
                                                    )
                                                }
                                            }
                                            item(key = "section_$sectionKey") {
                                                AnimatedVisibility(
                                                    visible = !hasMixedSections || isExpanded,
                                                    enter = expandVertically(),
                                                    exit = shrinkVertically()
                                                ) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            12.dp
                                                        )
                                                    ) {
                                                        sectionEntries.forEach { (key, arrivals) ->
                                                            val parts = key.split("|")
                                                            ArrivalCard(
                                                                numLigne = parts.getOrNull(0)
                                                                    ?: "?",
                                                                destination = parts.getOrNull(1)
                                                                    ?: "?",
                                                                couleurFond = arrivals.first().couleurFond,
                                                                couleurTexte = arrivals.first().couleurTexte,
                                                                ligneId = arrivals.first().idLigne,
                                                                times = arrivals,
                                                                initialExpoMode = list.size < 4,
                                                                forcedExpandState = forcedExpandState,
                                                                onLongClick = { onItemLongClick(key) },
                                                                onTimeClick = { timeIndex ->
                                                                    onArrivalTimeClick(
                                                                        key,
                                                                        timeIndex
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    GroupingMode.BY_DIRECTION -> {
                                        val directionSections = listOf(
                                            Pair(
                                                10,
                                                list.filter { (_, arrivals) -> arrivals.first().sensAller }),
                                            Pair(
                                                11,
                                                list.filter { (_, arrivals) -> !arrivals.first().sensAller })
                                        ).filter { (_, entries) -> entries.isNotEmpty() }

                                        val hasMixedDirections = directionSections.size > 1
                                        directionSections.forEach { (sectionKey, sectionEntries) ->
                                            val isExpanded = expandedSections[sectionKey] != false
                                            if (hasMixedDirections) {
                                                item(key = "dir_header_$sectionKey") {
                                                    DirectionSectionHeader(
                                                        sensAller = sectionKey == 10,
                                                        isExpanded = isExpanded,
                                                        onToggle = {
                                                            expandedSections[sectionKey] =
                                                                !isExpanded
                                                        }
                                                    )
                                                }
                                            }
                                            item(key = "dir_section_$sectionKey") {
                                                AnimatedVisibility(
                                                    visible = !hasMixedDirections || isExpanded,
                                                    enter = expandVertically(),
                                                    exit = shrinkVertically()
                                                ) {
                                                    Column(
                                                        verticalArrangement = Arrangement.spacedBy(
                                                            12.dp
                                                        )
                                                    ) {
                                                        sectionEntries.forEach { (key, arrivals) ->
                                                            val parts = key.split("|")
                                                            ArrivalCard(
                                                                numLigne = parts.getOrNull(0)
                                                                    ?: "?",
                                                                destination = parts.getOrNull(1)
                                                                    ?: "?",
                                                                couleurFond = arrivals.first().couleurFond,
                                                                couleurTexte = arrivals.first().couleurTexte,
                                                                ligneId = arrivals.first().idLigne,
                                                                times = arrivals,
                                                                initialExpoMode = list.size < 4,
                                                                forcedExpandState = forcedExpandState,
                                                                onLongClick = { onItemLongClick(key) },
                                                                onTimeClick = { timeIndex ->
                                                                    onArrivalTimeClick(
                                                                        key,
                                                                        timeIndex
                                                                    )
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    GroupingMode.NONE -> {
                                        list.forEach { (key, arrivals) ->
                                            item(key = "card_$key") {
                                                val parts = key.split("|")
                                                ArrivalCard(
                                                    numLigne = parts.getOrNull(0) ?: "?",
                                                    destination = parts.getOrNull(1) ?: "?",
                                                    couleurFond = arrivals.first().couleurFond,
                                                    couleurTexte = arrivals.first().couleurTexte,
                                                    ligneId = arrivals.first().idLigne,
                                                    times = arrivals,
                                                    initialExpoMode = list.size < 4,
                                                    forcedExpandState = forcedExpandState,
                                                    onLongClick = { onItemLongClick(key) },
                                                    onTimeClick = { timeIndex ->
                                                        onArrivalTimeClick(key, timeIndex)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    StopDetailsTab.NEARBY -> {
                        if (isLoadingNearbyStops && nearbyStops.isEmpty()) {
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
                        } else if (nearbyStops.isEmpty()) {
                            item {
                                Text(
                                    "Aucune station à proximité trouvée",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            item {
                                Column {
                                    nearbyStops.forEach { stop ->
                                        val hasVariants = stop.duplicates.size > 1
                                        BusStopItem(
                                            stop = stop,
                                            isFavorite = isNearbyStopFavorite(stop),
                                            groupDuplicates = hasVariants,
                                            onFillQuery = onFillQuery,
                                            onToggleFavorite = {
                                                onToggleNearbyFavorite(stop, !hasVariants)
                                            },
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

                    StopDetailsTab.VELOCITE -> {
                        if (velociteStation != null) {
                            item(key = "velocite_content") {
                                VelociteDetailsContent(
                                    station = velociteStation,
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalPadding = 0.dp,
                                    onClick = onVelociteClick
                                )
                            }
                        }
                    }
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
            },
            isGroupedFavorite = isNearbyStopFavorite(selectedStop!!),
            isDuplicateFavorite = { duplicate -> isNearbyDuplicateFavorite(duplicate) },
            onToggleGroupedFavorite = {
                onToggleNearbyFavorite(selectedStop!!, false)
            },
            onToggleDuplicateFavorite = { duplicate ->
                onToggleNearbyFavorite(duplicate, true)
            },
            onFillQuery = onFillQuery
        )
    }
}

@Composable
private fun TransportSectionHeader(
    mode: Int,
    subtitle: String? = null,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val label = when (mode) {
        0 -> "Bus"
        1 -> "Tram"
        2 -> "Lianes"
        3 -> "Vélocité"
        4 -> "Périurbain"
        5 -> "Scolaire"
        else -> "Autre"
    }
    val icon = when (mode) {
        1 -> Icons.Filled.Tram
        3 -> Icons.Filled.DirectionsBike
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
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) "Réduire" else "Développer"
        )
    }
}

@Composable
private fun DirectionSectionHeader(
    sensAller: Boolean,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val label = if (sensAller) "Aller" else "Retour"
    val icon =
        if (sensAller) Icons.AutoMirrored.Filled.ArrowForward else Icons.AutoMirrored.Filled.ArrowBack

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
