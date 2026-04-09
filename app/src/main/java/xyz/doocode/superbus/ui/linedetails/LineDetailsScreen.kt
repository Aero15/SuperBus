package xyz.doocode.superbus.ui.linedetails

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.zIndex
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.ginko.Ligne
import xyz.doocode.superbus.core.dto.ginko.Variante
import xyz.doocode.superbus.ui.lines.LineBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailsScreen(
    onNavigateBack: () -> Unit,
    onStopClick: (Arret) -> Unit,
    viewModel: LineDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.isSearching) {
        if (uiState.isSearching) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearching) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            placeholder = { Text("Rechercher une station") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        // Empty title as we have the header now
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isSearching) viewModel.toggleSearch() else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    if (uiState.isSearching) {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    } else {
                        IconButton(onClick = viewModel::toggleSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (!uiState.isSearching) {
                uiState.line?.let { line ->
                    LineIdentityFullHeader(line = line)
                }
            }

            // Variant Selector
            uiState.line?.let { line ->
                // If more than 3 variants, scrollable. Else fixed to fill width.
                if (line.variantes.size > 3) {
                    ScrollableTabRow(
                        selectedTabIndex = line.variantes.indexOfFirst { it.id == uiState.selectedVariante?.id }
                            .coerceAtLeast(0),
                        edgePadding = 0.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(
                                    tabPositions[line.variantes.indexOfFirst { it.id == uiState.selectedVariante?.id }
                                        .coerceAtLeast(0)]
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        VariantTabs(line.variantes, uiState.selectedVariante, viewModel)
                    }
                } else {
                    TabRow(
                        selectedTabIndex = line.variantes.indexOfFirst { it.id == uiState.selectedVariante?.id }
                            .coerceAtLeast(0),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(
                                    tabPositions[line.variantes.indexOfFirst { it.id == uiState.selectedVariante?.id }
                                        .coerceAtLeast(0)]
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        VariantTabs(line.variantes, uiState.selectedVariante, viewModel)
                    }
                }
            }

            // Stops List
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Column {
                        // Station count header
                        if (uiState.filteredStops.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${uiState.filteredStops.size} stations",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(
                                        vertical = 4.dp,
                                        horizontal = 16.dp
                                    ),
                                    textAlign = TextAlign.End
                                )
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            itemsIndexed(uiState.filteredStops) { index, stop ->
                                ItineraryStopItem(
                                    stop = stop,
                                    line = uiState.line,
                                    isFirst = index == 0,
                                    isLast = index == uiState.filteredStops.lastIndex,
                                    onClick = { onStopClick(stop) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VariantTabs(
    variantes: List<Variante>,
    selectedVariante: Variante?,
    viewModel: LineDetailsViewModel
) {
    variantes.forEach { variante ->
        Tab(
            selected = variante.id == selectedVariante?.id,
            onClick = { viewModel.onVariantSelected(variante) },
            text = {
                Text(
                    "Vers ${variante.destination}",
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        )
    }
}

@Composable
fun LineIdentityFullHeader(line: Ligne) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        // The "Enseigne" Badge - Positioned at TopCenter
        LineBadge(
            line = line,
            size = 56.dp, // Slightly smaller than previous to look nice with TopBar
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f) // Ensure it draws on top of the card
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 0.dp,
                    top = 40.dp // Push card down to make room for badge half-overlap
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 16.dp), // Check content not covered by badge
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = line.libellePublic,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                /*Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (line.modeTransport) {
                            0 -> Icons.Default.DirectionsSubway
                            else -> Icons.Default.DirectionsBus
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (line.typologie != 0) "Transport en commun" else "Ligne Régulière",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }*/

                if (line.scolaire || line.periurbain) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (line.scolaire) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Scolaire") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (line.periurbain) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("Périurbain") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun ItineraryStopItem(
    stop: Arret,
    line: Ligne?,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    val lineColor = try {
        val color = Color(AndroidColor.parseColor("#${line?.couleurFond ?: "000000"}"))
        if (color == Color.White) {
            Color(AndroidColor.parseColor("#${line?.couleurTexte ?: "000000"}"))
        } else {
            color
        }
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Timeline Column
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 4.dp.toPx()
                val centerX = size.width / 2
                val centerY = size.height / 2

                // Draw Top Line
                if (!isFirst) {
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, centerY),
                        strokeWidth = strokeWidth
                    )
                }

                // Draw Bottom Line
                if (!isLast) {
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, size.height),
                        strokeWidth = strokeWidth
                    )
                }

                // Draw Node
                val nodeRadius = 10.dp.toPx()
                val nodeStrokeWidth = 4.dp.toPx()

                if (isFirst || isLast) {
                    // Square for terminals
                    val squareSize = nodeRadius * 2
                    val topLeft = Offset(centerX - nodeRadius, centerY - nodeRadius)
                    val squareSizeObj = Size(squareSize, squareSize)

                    // Fill background
                    drawRoundRect(
                        color = lineColor,
                        topLeft = topLeft,
                        size = squareSizeObj,
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    // Stroke border
                    drawRoundRect(
                        color = surfaceColor,
                        topLeft = topLeft,
                        size = squareSizeObj,
                        style = Stroke(width = nodeStrokeWidth),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                } else {
                    // Circle for intermediates
                    // Fill background
                    drawCircle(
                        color = lineColor,
                        radius = nodeRadius,
                        center = Offset(centerX, centerY)
                    )
                    // Stroke border
                    drawCircle(
                        color = surfaceColor,
                        radius = nodeRadius,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = nodeStrokeWidth)
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 12.dp, horizontal = 8.dp)
        ) {
            val parts = stop.nom.split(" - ", limit = 2)
            if (parts.size == 2) {
                Text(
                    text = parts[0],
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Text(
                    text = parts[1],
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = stop.nom,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
