package xyz.doocode.superbus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Arret
import xyz.doocode.superbus.core.util.removeAccents

@Composable
fun StopListItem(
    stop: Arret,
    searchQuery: String = "",
    onClick: () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Highlight matching text
            val annotatedString = buildAnnotatedString {
                val fullText = stop.nom
                val query = searchQuery.trim()

                if (query.isEmpty()) {
                    append(fullText)
                } else {
                    val normalizedFullText = fullText.removeAccents()
                    val normalizedQuery = query.removeAccents()
                    val startIndex = normalizedFullText.indexOf(normalizedQuery, ignoreCase = true)

                    if (startIndex >= 0) {
                        val endIndex = startIndex + query.length
                        // Safe check for indices in original text just in case lengths differ
                        val safeEndIndex = endIndex.coerceAtMost(fullText.length)
                        val safeStartIndex = startIndex.coerceAtMost(safeEndIndex)

                        // Text before match
                        append(fullText.substring(0, safeStartIndex))

                        // Matched text
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            append(fullText.substring(safeStartIndex, safeEndIndex))
                        }

                        // Text after match
                        append(fullText.substring(safeEndIndex))
                    } else {
                        // Fallback
                        append(fullText)
                    }
                }
            }

            Text(
                text = annotatedString,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
