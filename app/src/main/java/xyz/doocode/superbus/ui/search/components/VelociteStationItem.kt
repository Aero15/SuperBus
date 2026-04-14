package xyz.doocode.superbus.ui.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.core.util.formatVelociteStationName
import xyz.doocode.superbus.core.util.removeAccents

@Composable
fun VelociteStationItem(
    station: Station,
    searchQuery: String = "",
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                contentDescription = "Vélocité",
                tint = Color(0xFF00AAC2)
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val cleanName = formatVelociteStationName(station.name)

                fun highlight(text: String): AnnotatedString = buildAnnotatedString {
                    val query = searchQuery.trim()
                    if (query.isEmpty()) {
                        append(text)
                    } else {
                        val normalizedText = text.removeAccents()
                        val normalizedQuery = query.removeAccents()
                        val startIndex = normalizedText.indexOf(normalizedQuery, ignoreCase = true)

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
                            ) { append(text.substring(safeStartIndex, safeEndIndex)) }
                            append(text.substring(safeEndIndex))
                        } else {
                            append(text)
                        }
                    }
                }

                Text(text = highlight(cleanName), style = MaterialTheme.typography.bodyLarge)
            }

            Text(
                text = "#${station.number}",
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
