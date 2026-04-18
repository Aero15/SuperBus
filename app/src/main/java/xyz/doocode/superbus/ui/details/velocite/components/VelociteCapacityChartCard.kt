package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
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

    Column/*(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp))*/ {
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
            val totalBikes = mechBikes + elecBikes

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalBikes.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 72.sp
                        ),
                        color = if (totalBikes == 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "vélos".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = if (totalBikes == 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        lineHeight = 12.sp
                    )
                }

                Text(
                    text = "/",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Light
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 26.dp)
                )

                Column {
                    Text(
                        text = capacity.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "bornes".uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        lineHeight = 12.sp
                    )
                }
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
                    value = unavailableStands,
                    striped = true
                )
            }

            if (unavailableStands > 0) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                ) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 10.dp, y = (-8).dp)
                                .size(88.dp)
                                .rotate(20f)
                        )

                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Pourquoi certaines bornes sont indisponibles ?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ces bornes indisponibles (de manière temporaire) peuvent l'être pour l'une de ces raisons :",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "• borne mise hors-service pour des travaux ou une maintenance\n" +
                                        "• borne occupée par un vélo électrique en cours de rechargement\n" +
                                        "• borne avec un vélo mal raccordé ou un faux contact",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
                counter = it + 1,
                isStriped = true
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
                            .then(
                                if (slot.isStriped) {
                                    Modifier.unavailableStripedBackground(baseColor = slot.color)
                                } else {
                                    Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(slot.color)
                                }
                            ),
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
    val counter: Int,
    val isStriped: Boolean = false
)

private fun Modifier.unavailableStripedBackground(
    baseColor: Color = UnavailableStandsColor,
    stripeColor: Color = Color(0xFFC0730D)
): Modifier =
    this
        .clip(RoundedCornerShape(6.dp))
        .background(baseColor)
        .drawWithCache {
            val stripeWidth = 4.dp.toPx()
            val stripeSpacing = 10.dp.toPx()

            onDrawBehind {
                var x = -size.height
                while (x < size.width + size.height) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(x, size.height),
                        end = Offset(x + size.height, 0f),
                        strokeWidth = stripeWidth
                    )
                    x += stripeSpacing
                }
            }
        }

@Composable
fun LegendItem(color: Color, label: String, value: Int, striped: Boolean = false) {
    val isZero = value == 0
    val displayColor = if (isZero) Color.Gray.copy(alpha = 0.5f) else color
    val textColor = if (isZero) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.onSurface
    val textDecoration = if (isZero) TextDecoration.LineThrough else null

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .then(
                        if (striped && !isZero) {
                            Modifier.unavailableStripedBackground(baseColor = displayColor)
                        } else {
                            Modifier.background(displayColor, RoundedCornerShape(3.dp))
                        }
                    )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            textDecoration = textDecoration,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            textDecoration = textDecoration,
            fontWeight = FontWeight.Bold
        )
    }
}
