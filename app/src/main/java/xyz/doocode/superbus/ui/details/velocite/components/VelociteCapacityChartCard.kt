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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.theme.AvailableStandsColor
import xyz.doocode.superbus.ui.theme.ElectricBikeColor
import xyz.doocode.superbus.ui.theme.MechanicalBikeColor
import xyz.doocode.superbus.ui.theme.UnavailableStandsColor

@Composable
fun VelociteCapacityChartCard(station: Station) {
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
            }

            if (capacity > 0) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = capacity.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "bornes vélo".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        lineHeight = 12.sp
                    )
                }

                VelociteCapacityGrid(
                    station = station,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    LegendItem(
                        color = MechanicalBikeColor, label = "Vélos mécaniques", value = mechBikes
                    )
                    LegendItem(
                        color = ElectricBikeColor, label = "Vélos électriques", value = elecBikes
                    )
                    LegendItem(
                        color = AvailableStandsColor,
                        label = "Places disponibles",
                        value = availableStands
                    )
                    LegendItem(
                        color = UnavailableStandsColor,
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
fun VelociteCapacityGrid(station: Station, modifier: Modifier = Modifier) {
    val mechBikes = station.totalStands.availabilities.mechanicalBikes
    val elecBikes = station.totalStands.availabilities.electricalBikes
    val availableStands = station.totalStands.availabilities.stands
    val capacity = station.totalStands.capacity
    val unavailableStands = maxOf(0, capacity - (mechBikes + elecBikes + availableStands))

    if (capacity <= 0) return

    val maxItemsPerRow = minOf(capacity, 10)
    var bestItemsPerRow = maxItemsPerRow
    var bestFillRatio = -1f
    var bestRowCount = Int.MAX_VALUE

    for (c in 1..maxItemsPerRow) {
        val lastRowItems = if (capacity % c == 0) c else capacity % c
        val fillRatio = lastRowItems.toFloat() / c.toFloat()
        val rowCount = (capacity + c - 1) / c
        val shouldSelect = rowCount < bestRowCount ||
                (rowCount == bestRowCount && fillRatio > bestFillRatio)

        if (shouldSelect) {
            bestFillRatio = fillRatio
            bestItemsPerRow = c
            bestRowCount = rowCount
        }
    }

    val itemsPerRow = bestItemsPerRow

    val totalSlots = mutableListOf<CapacitySlot>()
    repeat(mechBikes) {
        totalSlots.add(
            CapacitySlot(
                color = MechanicalBikeColor,
                counter = it + 1
            )
        )
    }
    repeat(elecBikes) {
        totalSlots.add(
            CapacitySlot(
                color = ElectricBikeColor,
                counter = it + 1
            )
        )
    }
    repeat(availableStands) {
        totalSlots.add(
            CapacitySlot(
                color = AvailableStandsColor,
                counter = it + 1
            )
        )
    }
    repeat(unavailableStands) {
        totalSlots.add(
            CapacitySlot(
                color = UnavailableStandsColor,
                counter = it + 1
            )
        )
    }

    val chunks = totalSlots.chunked(itemsPerRow)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        chunks.forEach { chunk ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                chunk.forEach { slot ->
                    val counterTextColor =
                        if (slot.color.luminance() < 0.5f) Color.White else Color.Black

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(slot.color),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = slot.counter.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = counterTextColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                val remaining = itemsPerRow - chunk.size
                repeat(remaining) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

private data class CapacitySlot(
    val color: Color,
    val counter: Int
)

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
