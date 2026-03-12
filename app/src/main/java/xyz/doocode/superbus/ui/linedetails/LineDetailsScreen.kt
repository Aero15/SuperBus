package xyz.doocode.superbus.ui.linedetails

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.dto.Ligne
import xyz.doocode.superbus.core.dto.Variante
import xyz.doocode.superbus.ui.lines.LineBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailsScreen(
    onNavigateBack: () -> Unit,
    onStopClick: (Arret) -> Unit,
    viewModel: LineDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearching) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Rechercher un arrêt...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            uiState.line?.let { line ->
                                LineBadge(line = line, size = 32.dp, fontSize = 14.sp)
                            }
                            Text(
                                text = " Détails",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
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
            // Variant Selector
            uiState.line?.let { line ->
                ScrollableTabRow(
                    selectedTabIndex = line.variantes.indexOfFirst { it.id == uiState.selectedVariante?.id }
                        .coerceAtLeast(0),
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[line.variantes.indexOfFirst { it.id == uiState.selectedVariante?.id }
                                .coerceAtLeast(0)]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    line.variantes.forEach { variante ->
                        Tab(
                            selected = variante.id == uiState.selectedVariante?.id,
                            onClick = { viewModel.onVariantSelected(variante) },
                            text = {
                                Text(
                                    "Vers ${variante.destination}",
                                    maxLines = 1
                                )
                            }
                        )
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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.filteredStops) { stop ->
                            StopItem(stop = stop, onClick = { onStopClick(stop) })
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopItem(
    stop: Arret,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(stop.nom, fontWeight = FontWeight.SemiBold) },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
