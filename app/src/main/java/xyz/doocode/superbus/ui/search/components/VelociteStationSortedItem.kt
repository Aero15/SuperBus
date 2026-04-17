package xyz.doocode.superbus.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.core.util.formatVelociteStationName
import xyz.doocode.superbus.core.util.removeAccents
import xyz.doocode.superbus.ui.search.VelociteSortField

@Composable
fun VelociteStationSortedItem(
    station: Station,
    sortField: VelociteSortField,
    searchQuery: String = "",
    onClick: () -> Unit = {}
) {
    val unavailableStands = maxOf(
        0,
        station.totalStands.capacity -
                (
                        station.mainStands.availabilities.mechanicalBikes +
                                station.mainStands.availabilities.electricalBikes +
                                station.mainStands.availabilities.stands
                        )
    )

    val isCountSort = sortField in setOf(
        VelociteSortField.AVAILABLE_STANDS,
        VelociteSortField.TOTAL_BIKES,
        VelociteSortField.MECHANICAL_BIKES,
        VelociteSortField.ELECTRICAL_BIKES,
        VelociteSortField.UNAVAILABLE_STANDS
    )

    val unavailableColor = when {
        unavailableStands == station.totalStands.capacity && station.totalStands.capacity > 0 -> Color(
            0xFFD32F2F
        )

        unavailableStands > 0 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    val totemIcon = when (sortField) {
        VelociteSortField.AVAILABLE_STANDS -> Icons.Filled.LocalParking
        VelociteSortField.ELECTRICAL_BIKES -> Icons.Filled.ElectricBike
        VelociteSortField.MECHANICAL_BIKES -> Icons.Filled.PedalBike
        VelociteSortField.UNAVAILABLE_STANDS ->
            when {
                unavailableStands == station.totalStands.capacity && station.totalStands.capacity > 0 -> Icons.Filled.Block
                unavailableStands > 0 -> Icons.Filled.Warning
                else -> Icons.Filled.CheckCircle
            }

        else -> Icons.AutoMirrored.Filled.DirectionsBike
    }

    val count = when (sortField) {
        VelociteSortField.AVAILABLE_STANDS -> station.mainStands.availabilities.stands
        VelociteSortField.TOTAL_BIKES -> station.mainStands.availabilities.bikes
        VelociteSortField.MECHANICAL_BIKES -> station.mainStands.availabilities.mechanicalBikes
        VelociteSortField.ELECTRICAL_BIKES -> station.mainStands.availabilities.electricalBikes
        VelociteSortField.UNAVAILABLE_STANDS -> unavailableStands
        else -> 0
    }

    val capacity = station.totalStands.capacity

    val countColor = when {
        count == 0 -> Color(0xFFD32F2F)
        count <= 2 -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    val bgColor = when {
        sortField == VelociteSortField.UNAVAILABLE_STANDS -> unavailableColor.copy(alpha = 0.2f)
        isCountSort -> countColor.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val tabularNumberStyle = TextStyle(
        fontFeatureSettings = "tnum",
        textAlign = TextAlign.End
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = totemIcon,
                contentDescription = "Vélocité",
                tint =
                    when {
                        sortField == VelociteSortField.UNAVAILABLE_STANDS -> unavailableColor
                        isCountSort -> countColor
                        else -> Color(0xFF00AAC2)
                    }
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

            Spacer(modifier = Modifier.width(8.dp))

            when (sortField) {
                VelociteSortField.CAPACITY -> {
                    Text(
                        text = "$capacity",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge.merge(tabularNumberStyle),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                VelociteSortField.AVAILABLE_STANDS,
                VelociteSortField.TOTAL_BIKES,
                VelociteSortField.MECHANICAL_BIKES,
                VelociteSortField.ELECTRICAL_BIKES,
                VelociteSortField.UNAVAILABLE_STANDS -> {
                    val sortIcon = when (sortField) {
                        VelociteSortField.AVAILABLE_STANDS -> Icons.Filled.LocalParking
                        VelociteSortField.ELECTRICAL_BIKES -> Icons.Filled.ElectricBike
                        VelociteSortField.MECHANICAL_BIKES -> Icons.Filled.PedalBike
                        VelociteSortField.UNAVAILABLE_STANDS -> totemIcon
                        else -> Icons.AutoMirrored.Filled.DirectionsBike
                    }
                    val valueColor =
                        if (sortField == VelociteSortField.UNAVAILABLE_STANDS) unavailableColor else countColor
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = sortIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = valueColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$count",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyLarge.merge(tabularNumberStyle),
                            color = valueColor
                        )
                        Text(
                            text = " / ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "$capacity",
                            style = MaterialTheme.typography.bodyMedium.merge(tabularNumberStyle),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                else -> {
                    Text(
                        text = "#${station.number}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.bodyLarge.merge(tabularNumberStyle),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
