package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.ui.theme.headerGradientColors

@Composable
fun CollapsingHeader(
    stopName: String,
    collapseFraction: Float,
    maxHeight: Dp,
    minHeight: Dp,
    offset: Float,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
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

    // Calculate current height based on collapse fraction
    val height = with(density) {
        androidx.compose.ui.util.lerp(minHeight.toPx(), maxHeight.toPx(), t).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        // --- Sticky Elements Container ---

        // --- Expanded Content (Title Label & Floating Action) (Fade Out) ---
        // We put non-morphing static elements here
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarHeight)
                .graphicsLayer { alpha = 1f - collapseFraction }
        ) {
            // Expanded Favorite Button (Top Right)
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 4.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                    tint = if (isFavorite) Color.Yellow else Color.White
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 100.dp) // Above text position approx
            ) {
                Text(
                    text = "Station",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
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

        // --- Menu & Favorite Button (Fade In) ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp, end = 4.dp)
                .graphicsLayer { alpha = t },
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                    tint = if (isFavorite) Color.Yellow else Color.White
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
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
