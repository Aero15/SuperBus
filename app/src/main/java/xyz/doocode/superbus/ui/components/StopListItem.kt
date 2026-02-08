package xyz.doocode.superbus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.dto.LineInfo
import xyz.doocode.superbus.core.util.removeAccents
import xyz.doocode.superbus.ui.favorites.SmallLineBadge

@Composable
fun StopListItem(
    stop: Arret,
    searchQuery: String = "",
    isFavorite: Boolean = false,
    favoriteLines: List<LineInfo> = emptyList(),
    onFillQuery: (String) -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Highlight matching text
                val annotatedString = buildAnnotatedString {
                    val fullText = stop.nom
                    val query = searchQuery.trim()

                    if (query.isEmpty()) {
                        append(fullText)
                    } else {
                        val normalizedFullText = fullText.removeAccents()
                        val normalizedQuery = query.removeAccents()
                        val startIndex =
                            normalizedFullText.indexOf(normalizedQuery, ignoreCase = true)

                        if (startIndex >= 0) {
                            val endIndex = startIndex + query.length
                            val safeEndIndex = endIndex.coerceAtMost(fullText.length)
                            val safeStartIndex = startIndex.coerceAtMost(safeEndIndex)

                            append(fullText.substring(0, safeStartIndex))
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                append(fullText.substring(safeStartIndex, safeEndIndex))
                            }
                            append(fullText.substring(safeEndIndex))
                        } else {
                            append(fullText)
                        }
                    }
                }

                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (isFavorite && favoriteLines.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        favoriteLines.forEach { line ->
                            SmallLineBadge(line = line)
                        }
                    }
                }
            }

            // Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onFillQuery(stop.nom) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Insérer ce nom"
                    )
                }

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoris",
                        tint = if (isFavorite) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
