package xyz.doocode.superbus.ui.lines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.Ligne

@Composable
fun LinesGrid(
    groupedLines: Map<String, List<Ligne>>,
    onLineClick: (Ligne) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        groupedLines.forEach { (category, lines) ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 8.dp)
                        .fillMaxWidth()
                )
            }
            items(lines) { line ->
                LineCard(line = line, onClick = { onLineClick(line) })
            }
        }
    }
}

@Composable
fun LineCard(
    line: Ligne,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line Badges (Number)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(parseColorSafe(line.couleurFond))
            ) {
                Text(
                    text = line.numLignePublic,
                    color = parseColorSafe(line.couleurTexte),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Line Name/Descriptions
            Text(
                text = line.libellePublic,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
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
