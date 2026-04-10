package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.theme.AvailableStandsYellow
import xyz.doocode.superbus.ui.theme.ElectricBikeGreen
import xyz.doocode.superbus.ui.theme.MechanicalBikeBlue

@Composable
fun VelociteCapacityChartCard(station: Station) {
    val totalBikes = station.totalStands.availabilities.bikes
    val availableStands = station.totalStands.availabilities.stands
    val mechBikes = station.totalStands.availabilities.mechanicalBikes
    val elecBikes = station.totalStands.availabilities.electricalBikes
    val capacity = station.totalStands.capacity
    val unavailableStands = maxOf(0, capacity - (mechBikes + elecBikes + availableStands))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Répartition détaillée",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Visualisez la capacité de la station, ainsi que les bornes hors service ou mal enclenchées.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (capacity > 0) {
                Text(
                    text = "$capacity bornes vélo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 6.dp)
                )

                val minCols = if (capacity > 20) 10 else capacity
                val maxCols = minOf(capacity, 20)
                var bestItemsPerRow = minCols
                var bestFillRatio = -1f

                for (c in minCols..maxCols) {
                    val lastRowItems = if (capacity % c == 0) c else capacity % c
                    val fillRatio = lastRowItems.toFloat() / c.toFloat()
                    if (fillRatio >= bestFillRatio) {
                        bestFillRatio = fillRatio
                        bestItemsPerRow = c
                    }
                }

                val itemsPerRow = bestItemsPerRow

                val totalSlots = mutableListOf<Color>()
                repeat(mechBikes) { totalSlots.add(MechanicalBikeBlue) }
                repeat(elecBikes) { totalSlots.add(ElectricBikeGreen) }
                repeat(availableStands) { totalSlots.add(AvailableStandsYellow) }
                repeat(unavailableStands) { totalSlots.add(Color(0xFFE53935)) }

                val chunks = totalSlots.chunked(itemsPerRow)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    chunks.forEach { chunk ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            chunk.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(color)
                                )
                            }

                            val remaining = itemsPerRow - chunk.size
                            repeat(remaining) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LegendItem(
                        color = MechanicalBikeBlue, label = "Vélos mécaniques", value = mechBikes
                    )
                    LegendItem(
                        color = ElectricBikeGreen, label = "Vélos électriques", value = elecBikes
                    )
                    LegendItem(
                        color = AvailableStandsYellow,
                        label = "Places disponibles",
                        value = availableStands
                    )
                    LegendItem(
                        color = Color(0xFFE53935),
                        label = "Hors service / Non dispo.",
                        value = unavailableStands
                    )
                }
            } else {
                Text(
                    text = "Capacité inconnue.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: Int) {
    val isZero = value == 0
    val displayColor = if (isZero) Color.Gray.copy(alpha = 0.5f) else color
    val textColor = if (isZero) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.onSurface
    val textDecoration = if (isZero) TextDecoration.LineThrough else null

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(displayColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textDecoration = textDecoration,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textDecoration = textDecoration,
            fontWeight = FontWeight.Bold
        )
    }
}
