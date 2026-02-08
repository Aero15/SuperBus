package xyz.doocode.superbus.ui.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.theme.headerGradientColors
import kotlin.math.roundToInt

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
                        top = maxHeaderHeight + 16.dp, // Content starts after max header
                        bottom = 16.dp,
                        start = 16.dp,
                        end = 16.dp
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
                height = maxHeaderHeight,
                offset = headerOffset,
                onBackClick = onBackClick,
                onMenuClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun CollapsingHeader(
    stopName: String,
    collapseFraction: Float,
    height: androidx.compose.ui.unit.Dp,
    offset: Float,
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val gradientColors = headerGradientColors(isDark)

    // Morphing Calculations
    val density = LocalDensity.current
    val t = collapseFraction

    // Expanded State Constants
    val expandedPaddingStart = 24.dp
    val expandedPaddingBottom = 60.dp // Space for chips

    // Collapsed State Constants (Target)
    val toolbarHeight = 64.dp

    // Text Dimensions and Scaling
    var textWidth by remember { mutableFloatStateOf(0f) }
    var textHeight by remember { mutableFloatStateOf(0f) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx() }

    // Calculate Scale
    // Expanded: DisplayMedium (size ~45sp)
    // Collapsed: TitleLarge (size ~22sp)
    val scaleTarget = 0.6f
    val currentScale = androidx.compose.ui.util.lerp(1f, scaleTarget, t)

    // Calculate Positions (Relative to BottomStart of the Header Box)
    // Expanded Position (Start)
    val expandedX = with(density) { expandedPaddingStart.toPx() }
    val expandedYFromBottom = with(density) { expandedPaddingBottom.toPx() }

    // Collapsed Position (End)
    // Center horizontally
    val collapsedX = (screenWidthPx - (textWidth * scaleTarget)) / 2f

    // Center vertically in the toolbar height
    val toolbarHeightPx = with(density) { toolbarHeight.toPx() }
    val collapsedPaddingBottom = (toolbarHeightPx - textHeight * scaleTarget) / 2f

    // Delta Calculation
    // We use translation to move from Expanded to Collapsed.
    val deltaX = (collapsedX - expandedX) * t
    val deltaY = (expandedYFromBottom - collapsedPaddingBottom) * t

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .offset { IntOffset(x = 0, y = offset.roundToInt()) }
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        // --- Sticky Elements Container ---

        // --- Actions / Chips (Fade Out) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
                .graphicsLayer {
                    alpha = (1f - t * 2).coerceIn(0f, 1f)
                }
        ) {
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

        // --- Back Button (Crossfade / Move) ---
        // Expanding Button (Top Left)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 4.dp)
                .graphicsLayer { alpha = 1f - t }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White
            )
        }

        // Collapsed Button (Bottom Left - Sticky)
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp + 4.dp, start = 4.dp) // Vertically centered in 64dp toolbar
                .graphicsLayer { alpha = t }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = Color.White
            )
        }

        // --- Menu Button (Fade In) ---
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 4.dp)
                .graphicsLayer { alpha = t }
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Menu",
                tint = Color.White
            )
        }

        // --- Morphing Title ---
        Text(
            text = stopName,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = {
                textWidth = it.size.width.toFloat()
                textHeight = it.size.height.toFloat()
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = expandedPaddingStart, bottom = expandedPaddingBottom)
                .graphicsLayer {
                    scaleX = currentScale
                    scaleY = currentScale
                    translationX = deltaX
                    translationY = deltaY
                    // Pivot relative to TopLeft of component for predictable placement
                    transformOrigin = TransformOrigin(0f, 0f)
                }
        )
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

