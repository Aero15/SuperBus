package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.ui.search.components.BusStopItem

@Composable
fun VelociteNearbyStops(
    nearbyStops: List<Arret>,
    isLoading: Boolean,
    isFavorite: (Arret) -> Boolean,
    onFillQuery: (String) -> Unit,
    onToggleFavorite: (Arret, Boolean) -> Unit,
    onStopClick: (Arret, Boolean) -> Unit,
    onVariantsClick: (Arret) -> Unit,
    onShowMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isLoading && nearbyStops.isEmpty()) return

    val visibleNearbyStops = nearbyStops.take(3)
    val remainingCount = (nearbyStops.size - visibleNearbyStops.size).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ) {
            Box(
                modifier = Modifier.padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Bus et trams à proximité",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    }
                } else {
                    visibleNearbyStops.forEach { stop ->
                        val hasVariants = stop.duplicates.size > 1
                        BusStopItem(
                            stop = stop,
                            isFavorite = isFavorite(stop),
                            groupDuplicates = hasVariants,
                            onFillQuery = onFillQuery,
                            onToggleFavorite = {
                                onToggleFavorite(stop, !hasVariants)
                            },
                            onClick = {
                                onStopClick(stop, !hasVariants)
                            },
                            onVariantsClick = { onVariantsClick(stop) }
                        )
                    }

                    if (remainingCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = onShowMoreClick,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(text = "+$remainingCount résultats supplémentaires")
                            }
                        }
                    }
                }
            }
        }
    }
}
