package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.FavoriteStation

@Composable
fun FavoriteTile(
    station: FavoriteStation,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .clickable(onClick = onClick)
    ) {
        // Tile Box
        Box(
            modifier = Modifier
                .aspectRatio(1f) // Square
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            val validLines = station.lines

            if (validLines.isEmpty()) {
                Text(
                    "?",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (validLines.size == 1) {
                // Single Large Badge
                val line = validLines.first()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EnlargedLineBadge(line)
                }
            } else {
                // Grid 2x2
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    val row1 = validLines.take(2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row1.forEach { SmallLineBadge(it) }
                        if (row1.size < 2) Spacer(modifier = Modifier.size(32.dp))
                    }

                    val remainingCount = validLines.size - 2
                    if (remainingCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Slot 3
                            val line3 = validLines[2]
                            SmallLineBadge(line3)

                            if (validLines.size == 4) {
                                SmallLineBadge(validLines[3])
                            } else if (validLines.size > 4) {
                                // Counter Badge
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(4.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${validLines.size - 3}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                // Empty slot (size was 3)
                                Spacer(modifier = Modifier.size(32.dp))
                            }
                        }
                    } else {
                        // Empty row 2
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Station Name
        Text(
            text = station.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
