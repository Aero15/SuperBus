package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.ErrorView
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
    val isFavorite by viewModel.isFavorite.collectAsState()
    val displayName = stopName ?: "Arrêt inconnu"

    // --- Collapsing Header Logic ---
    val density = LocalDensity.current
    val minHeaderHeight = 64.dp + WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding() // TopBar height + Status Bar
    val maxHeaderHeight = 260.dp

    val minHeaderHeightPx = with(density) { minHeaderHeight.toPx() }
    val maxHeaderHeightPx = with(density) { maxHeaderHeight.toPx() }

    // Offset varies from 0 (Expanded) to -(max - min) (Collapsed)
    var headerOffset by remember { mutableFloatStateOf(0f) }

    val maxOffset = minHeaderHeightPx - maxHeaderHeightPx

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val newOffset = (headerOffset + delta).coerceIn(maxOffset, 0f)
                headerOffset = newOffset
                // If we are in the middle of collapsing, we consume the scroll so the list doesn't scroll yet
                // But standard CoordinatorLayout behavior usually lets the list scroll only after header is done?
                // Actually, standard behavior:
                // Scroll Down (List at top): Expand Header first.
                // Scroll Up: Collapse Header first, then scroll list.

                // Let's implement "Scroll Up collapses header".
                return if (headerOffset != maxOffset && headerOffset != 0f) {
                    // We consumed it partially?
                    // Returning Offset.Zero allows the list to scroll "under" possibly?
                    // No, properly consuming it is better for the sticky effect.
                    // But connecting it perfectly is complex.
                    // Simplified approach: Don't consume, just transparently move header.
                    Offset.Zero
                } else {
                    Offset.Zero
                }
            }
        }
    }

    // Normalized collapse fraction (0.0 = Expanded, 1.0 = Collapsed)
    val collapseFraction = (headerOffset / maxOffset).coerceIn(0f, 1f)

    // -------------------------------

    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        contentWindowInsets = WindowInsets.navigationBars, // Handle bottom nav
    ) { _ -> // Ignore scaffold padding, we handle it
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                // Content List
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (LocalConfiguration.current.screenWidthDp > 600) 2 else 1),
                    contentPadding = PaddingValues(
                        top = maxHeaderHeight + 8.dp, // Content starts after max header
                        bottom = 8.dp,
                        start = 8.dp,
                        end = 8.dp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Translate the list up as we collapse the header
                            // This creates the effect that the list is pushing the header
                            translationY = headerOffset
                        },
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (val state = uiState) {
                        is StopDetailsUiState.Loading -> {
                            item { LoadingView("Chargement des horaires...") }
                        }

                        is StopDetailsUiState.Error -> {
                            item { ErrorView(state.message) { viewModel.init(stopName, stopId) } }
                        }

                        is StopDetailsUiState.Empty -> {
                            item { EmptyDataView() }
                        }

                        is StopDetailsUiState.Success -> {
                            items(state.groupedArrivals.toList()) { (key, arrivals) ->
                                val parts = key.split("|")
                                ArrivalCard(
                                    numLigne = parts.getOrNull(0) ?: "?",
                                    destination = parts.getOrNull(1) ?: "?",
                                    couleurFond = arrivals.first().couleurFond,
                                    couleurTexte = arrivals.first().couleurTexte,
                                    times = arrivals
                                )
                            }
                        }
                    }
                }
            }

            // Collapsing Header Overlay
            CollapsingHeader(
                stopName = displayName,
                collapseFraction = collapseFraction,
                maxHeight = maxHeaderHeight,
                minHeight = minHeaderHeight,
                offset = headerOffset,
                isFavorite = isFavorite,
                onBackClick = onBackClick,
                onFavoriteClick = viewModel::toggleFavorite,
                onMenuClick = { /* TODO */ }
            )
        }
    }
}

