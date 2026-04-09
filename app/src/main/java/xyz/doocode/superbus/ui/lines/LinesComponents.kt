package xyz.doocode.superbus.ui.lines

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.ginko.Ligne
import xyz.doocode.superbus.core.dto.ginko.Variante
import xyz.doocode.superbus.ui.components.BadgeBoxContent
import xyz.doocode.superbus.ui.components.BadgeContent
import xyz.doocode.superbus.ui.components.resolveBadgeContent

@Composable
fun LinesGrid(
    groupedLines: Map<String, List<Ligne>>,
    collapsedSections: Set<String>,
    onToggleSection: (String) -> Unit,
    onLineClick: (Ligne) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        groupedLines.forEach { (category, lines) ->
            item(key = category) {
                LineGroupSection(
                    title = category,
                    lines = lines,
                    isExpanded = !collapsedSections.contains(category),
                    onToggle = { onToggleSection(category) },
                    onLineClick = onLineClick
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineGroupSection(
    title: String,
    lines: List<Ligne>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onLineClick: (Ligne) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column {
            LineGroupHeader(
                title = title,
                count = lines.size,
                isExpanded = isExpanded,
                onToggle = onToggle
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                // Using FlowRow to arrange badges in a grid-like manner
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    lines.forEach { line ->
                        LineCard(line = line, onClick = { onLineClick(line) })
                    }
                }
            }
        }
    }
}

@Composable
fun LineGroupHeader(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ArrowRotation"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(16.dp), // Increased padding for taller header
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon based on title
        Icon(
            imageVector = getCategoryIcon(title),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(32.dp)
                .padding(end = 6.dp)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Count badge
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = if (isExpanded) "Réduire" else "Détails",
            modifier = Modifier.rotate(rotationState)
        )
    }
}

@Composable
fun LineCard(
    line: Ligne,
    onClick: () -> Unit
) {
    LineBadge(
        line = line,
        onClick = onClick,
        size = 56.dp
    )
}

@Composable
fun LineBadge(
    line: Ligne,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    size: Dp = 48.dp,
    fontSize: TextUnit = 16.sp
) {
    val content = resolveBadgeContent(line.id, line.numLignePublic)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(parseColorSafe(line.couleurFond))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        BadgeBoxContent(
            content = content,
            textColor = parseColorSafe(line.couleurTexte),
            fontSize = fontSize
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineVariantsSheetContent(
    line: Ligne,
    onVariantClick: (Variante) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Line Identity Badge (Big)
        LineBadge(line = line, size = 80.dp, fontSize = 32.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = line.libellePublic,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Variantes",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            modifier = Modifier.weight(
                1f,
                fill = false
            ), // Allow content to fit but not expand infinitely if not needed, or just let it scroll. BottomSheet handles scrolling.
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(line.variantes) { variante ->
                VariantItem(variante = variante, onClick = { onVariantClick(variante) })
            }
        }
    }
}

@Composable
fun VariantItem(
    variante: Variante,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Vers ${variante.destination}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (variante.precisionDestination.isNotEmpty()) {
                    Text(
                        text = variante.precisionDestination,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

fun parseColorSafe(colorString: String): Color {
    return try {
        val color = if (colorString.startsWith("#")) colorString else "#$colorString"
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        Color.Gray // Fallback
    }
}

fun getCategoryIcon(title: String): ImageVector {
    return when {
        title.contains("Tramway", ignoreCase = true) -> Icons.Default.DirectionsTransit
        title.contains("Scolaire", ignoreCase = true) -> Icons.Default.School
        title.contains("demande", ignoreCase = true) -> Icons.Default.LocalTaxi
        title.contains("Autre", ignoreCase = true) -> Icons.Default.QuestionMark
        else -> Icons.Default.DirectionsBus
    }
}
