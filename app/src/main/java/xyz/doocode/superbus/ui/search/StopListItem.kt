package xyz.doocode.superbus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import xyz.doocode.superbus.ui.components.StopActionsContainer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.dto.Ligne
import xyz.doocode.superbus.core.util.removeAccents
import xyz.doocode.superbus.ui.favorites.SmallLineBadge
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@Composable
fun StopListItem(
    stop: Arret,
    searchQuery: String = "",
    isFavorite: Boolean = false,
    favoriteLines: List<Ligne> = emptyList(),
    groupDuplicates: Boolean = true,
    onFillQuery: (String) -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit = {},
    onVariantsClick: () -> Unit = {},
    onDuplicateClick: (Arret) -> Unit = {}
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = { showContextMenu = true }
                    )
                }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isGare = stop.nom.contains("Gare", ignoreCase = true)
            val allTram = stop.duplicates.size > 1 && stop.duplicates.all { it.id.startsWith("t_") }
            val icon = when {
                isFavorite -> Icons.Default.Favorite
                isGare -> Icons.Default.Train
                allTram -> Icons.Default.Tram
                stop.duplicates.size > 1 -> Icons.Default.ManageSearch
                stop.id.startsWith("t_") -> Icons.Default.Tram
                else -> Icons.Default.DirectionsBus
            }
            val iconTint = when {
                isFavorite -> Color(0xFFE91E63)
                isGare -> Color(0xFFBC2CD2)
                allTram -> Color(0xFF4CAF50)
                stop.duplicates.size > 1 -> Color(0xFFFFB300)
                stop.id.startsWith("t_") -> Color(0xFF4CAF50)
                else -> Color(0xFFFF6D00)
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Highlight matching text and split line if needed
                val primaryColor = MaterialTheme.colorScheme.primary

                fun highlight(text: String): AnnotatedString = buildAnnotatedString {
                    val query = searchQuery.trim()

                    if (query.isEmpty()) {
                        append(text)
                    } else {
                        val normalizedText = text.removeAccents()
                        val normalizedQuery = query.removeAccents()
                        val startIndex =
                            normalizedText.indexOf(normalizedQuery, ignoreCase = true)

                        if (startIndex >= 0) {
                            val endIndex = startIndex + query.length
                            val safeEndIndex = endIndex.coerceAtMost(text.length)
                            val safeStartIndex = startIndex.coerceAtMost(safeEndIndex)

                            append(text.take(safeStartIndex))
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor
                                )
                            ) {
                                append(text.substring(safeStartIndex, safeEndIndex))
                            }
                            append(text.substring(safeEndIndex))
                        } else {
                            append(text)
                        }
                    }
                }

                val parts = stop.nom.split(" - ", limit = 2)
                if (parts.size == 2) {
                    Text(
                        text = highlight(parts[0]),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = highlight(parts[1]),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    Text(
                        text = highlight(stop.nom),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // TODO: Display data from cache
                /*if (isFavorite && favoriteLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        favoriteLines.forEach { line ->
                            SmallLineBadge(line = line)
                        }
                    }
                }*/
            }

            // Indicator for grouped duplicates
            if (groupDuplicates && stop.duplicates.size > 1) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.clickable { onVariantsClick() }
                ) {
                    Text(
                        text = "${stop.duplicates.size} variantes",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            } else {
                // Afficher l'id de la station
                Text(
                    text = "#${stop.id}",
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = 0.6f
                    )
                )
            }

            StopActionsContainer(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                stopName = stop.nom,
                stopId = stop.id,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onFillQuery = onFillQuery
            )
        }

        // Expanded list for non-grouped mode
        if (!groupDuplicates && stop.duplicates.isNotEmpty() && stop.duplicates.size > 1) {
            stop.duplicates.forEach { duplicate ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDuplicateClick(duplicate) }
                        .padding(start = 56.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Station #${duplicate.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (duplicate != stop.duplicates.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Preview(
    name = "Light mode",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true
)
@Preview(
    name = "Dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
fun StopListItemPreview() {
    SuperBusTheme {
        StopListItem(
            stop = Arret(
                id = "1",
                nom = "Gare Centrale",
                latitude = 0.0,
                longitude = 0.0,
                accessibilite = 0
            ),
            searchQuery = "gare",
            isFavorite = true,
            favoriteLines = listOf(
                Ligne(
                    id = "T1",
                    numLignePublic = "T1",
                    libellePublic = "Ligne 1",
                    couleurFond = "#0076bb",
                    couleurTexte = "#FFFFFF",
                    modeTransport = 0,
                    typologie = 0,
                    scolaire = false,
                    periurbain = false,
                    variantes = emptyList()
                ),
            )
        )
    }
}