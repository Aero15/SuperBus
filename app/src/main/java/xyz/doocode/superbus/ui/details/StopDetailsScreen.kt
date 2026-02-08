package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.components.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDetailsScreen(
    stopName: String?,
    stopId: String?,
    onBackClick: () -> Unit,
    viewModel: StopDetailsViewModel = viewModel()
) {
    LaunchedEffect(stopName, stopId) {
        viewModel.init(stopName, stopId)
    }

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val displayName = stopName ?: "Arrêt inconnu"

    Scaffold(
        topBar = {
            // Transparent top bar for back button over the header
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        // We use a box to let the header sit behind the top bar (edge-to-edge effect)
        // But we need to handle content padding manually for the rest

        val pullRefreshState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // MASSIVE HEADER
                StopDetailsHeader(stopName = displayName)

                // CONTENT
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (val state = uiState) {
                        is StopDetailsUiState.Loading -> LoadingView("Chargement des horaires...")
                        is StopDetailsUiState.Error -> ErrorView(state.message) {
                            viewModel.init(
                                stopName,
                                stopId
                            )
                        }

                        is StopDetailsUiState.Empty -> EmptyDataView()
                        is StopDetailsUiState.Success -> {
                            ArrivalsContent(groups = state.groupedArrivals)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StopDetailsHeader(stopName: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Massive height
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        ) {
            Text(
                text = "Station",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = stopName,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Placeholder for future actions
            Row {
                SuggestionChip(
                    onClick = { /* TODO */ },
                    label = { Text("Voir sur la carte") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        labelColor = Color.White
                    ),
                    border = null
                )
            }
        }
    }
}

@Composable
fun ArrivalsContent(groups: Map<String, List<Temps>>) {
    // Responsive logic: Grid for Tablet/Landscape, List for Phone/Portrait
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val columns = if (screenWidth > 600.dp) 2 else 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(groups.toList()) { (key, arrivals) ->
            // Key is "NumLine|Destination"
            val parts = key.split("|")
            val numLigne = parts.getOrNull(0) ?: "?"
            val destination = parts.getOrNull(1) ?: "?"

            // Getting color info from the first item
            val firstItem = arrivals.first()

            ArrivalCard(
                numLigne = numLigne,
                destination = destination,
                couleurFond = firstItem.couleurFond,
                couleurTexte = firstItem.couleurTexte,
                times = arrivals
            )
        }
    }
}

@Composable
fun ArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LineBadge(
                    numLigne = numLigne,
                    couleurFond = couleurFond,
                    couleurTexte = couleurTexte
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                times.take(3).forEach { temps ->
                    TimeDisplay(temps)
                }
            }
        }
    }
}

@Composable
fun TimeDisplay(temps: Temps) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayTime = temps.temps // "1 min", "15:42", etc.
        val isRealTime = temps.fiable

        Text(
            text = displayTime,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (!isRealTime) {
            Text(
                text = "Théorique",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
