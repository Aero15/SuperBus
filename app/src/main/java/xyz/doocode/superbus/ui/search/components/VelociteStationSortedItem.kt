package xyz.doocode.superbus.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import xyz.doocode.superbus.ui.components.StopActionsContainer
import xyz.doocode.superbus.ui.search.VelociteSortField

@Composable
fun VelociteStationSortedItem(
    station: Station,
    sortField: VelociteSortField,
    searchQuery: String = "",
    isFavorite: Boolean = false,
    onFillQuery: (String) -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var showContextMenu by remember { mutableStateOf(false) }
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
    val isStatusSort = sortField in setOf(
        VelociteSortField.BONUS,
        VelociteSortField.BANKING,
        VelociteSortField.OPEN,
        VelociteSortField.CONNECTED
    )

    val orangeColor = Color(0xFFFF9800)
    val greenColor = Color(0xFF4CAF50)
    val redColor = Color(0xFFD32F2F)
    val blueColor = Color(0xFF00AAC2)

    val unavailableColor = when {
        unavailableStands == station.totalStands.capacity && station.totalStands.capacity > 0 -> redColor
        unavailableStands > 0 -> orangeColor
        else -> greenColor
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
    val isBonus = station.bonus
    val hasBanking = station.banking
    val isOpen = station.status == "OPEN"
    val isConnected = station.connected

    val countColor = when {
        count == 0 -> redColor
        count <= 2 -> orangeColor
        else -> greenColor
    }

    val statusPositive = when (sortField) {
        VelociteSortField.BONUS -> isBonus
        VelociteSortField.BANKING -> hasBanking
        VelociteSortField.OPEN -> isOpen
        VelociteSortField.CONNECTED -> isConnected
        else -> false
    }

    val statusSortIcon = when (sortField) {
        VelociteSortField.BONUS -> if (statusPositive) Icons.Filled.AutoAwesome else Icons.AutoMirrored.Filled.DirectionsBike
        VelociteSortField.BANKING -> if (statusPositive) Icons.Filled.CreditCard else Icons.AutoMirrored.Filled.DirectionsBike
        VelociteSortField.OPEN -> if (statusPositive) Icons.Filled.CheckCircle else Icons.Filled.Block
        VelociteSortField.CONNECTED -> if (statusPositive) Icons.Filled.Wifi else Icons.Filled.Block
        else -> totemIcon
    }

    val statusColor = when (sortField) {
        VelociteSortField.BONUS -> if (statusPositive) orangeColor else blueColor
        VelociteSortField.BANKING -> if (statusPositive) greenColor else blueColor
        VelociteSortField.OPEN,
        VelociteSortField.CONNECTED -> if (statusPositive) greenColor else redColor

        else -> blueColor
    }

    val bgColor = when {
        sortField == VelociteSortField.BONUS -> if (isBonus) orangeColor.copy(alpha = 0.2f) else Color.Transparent
        sortField == VelociteSortField.BANKING -> if (hasBanking) greenColor.copy(alpha = 0.2f) else Color.Transparent
        sortField == VelociteSortField.OPEN -> if (isOpen) greenColor.copy(alpha = 0.2f) else redColor.copy(
            alpha = 0.2f
        )

        sortField == VelociteSortField.CONNECTED -> if (isConnected) greenColor.copy(alpha = 0.2f) else redColor.copy(
            alpha = 0.2f
        )

        sortField == VelociteSortField.UNAVAILABLE_STANDS -> unavailableColor.copy(alpha = 0.2f)
        isCountSort -> countColor.copy(alpha = 0.2f)
        else -> Color.Transparent
    }
    val leadingIcon =
        if (isFavorite) Icons.Default.Favorite else if (isStatusSort) statusSortIcon else totemIcon
    val leadingTint =
        if (isFavorite) {
            Color(0xFFE91E63)
        } else if (isStatusSort) {
            statusColor
        } else {
            when {
                sortField == VelociteSortField.UNAVAILABLE_STANDS -> unavailableColor
                isCountSort -> countColor
                else -> blueColor
            }
        }
    val cleanName = formatVelociteStationName(station.name)
    val tabularNumberStyle = TextStyle(
        fontFeatureSettings = "tnum",
        textAlign = TextAlign.End
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showContextMenu = true }
                )
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Vélocité",
                tint = leadingTint
            )
            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val primaryColor = MaterialTheme.colorScheme.primary

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

                VelociteSortField.BONUS,
                VelociteSortField.BANKING,
                VelociteSortField.OPEN,
                VelociteSortField.CONNECTED -> {
                    val statusIcon = when (sortField) {
                        VelociteSortField.BONUS -> if (statusPositive) Icons.Filled.AutoAwesome else Icons.Filled.Block
                        VelociteSortField.BANKING -> if (statusPositive) Icons.Filled.CreditCard else Icons.Filled.Block
                        VelociteSortField.OPEN -> if (statusPositive) Icons.Filled.CheckCircle else Icons.Filled.Block
                        VelociteSortField.CONNECTED -> if (statusPositive) Icons.Filled.Wifi else Icons.Filled.Block
                        else -> Icons.Filled.Block
                    }
                    val valueColor = when (sortField) {
                        VelociteSortField.BONUS -> if (statusPositive) orangeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        VelociteSortField.BANKING -> if (statusPositive) greenColor else MaterialTheme.colorScheme.onSurfaceVariant
                        VelociteSortField.OPEN,
                        VelociteSortField.CONNECTED -> if (statusPositive) greenColor else redColor

                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    val statusLabel = when (sortField) {
                        VelociteSortField.BONUS -> if (statusPositive) "Bonus" else "Non bonus"
                        VelociteSortField.BANKING -> if (statusPositive) "Disponible" else "Sans TPE"
                        VelociteSortField.OPEN -> if (statusPositive) "Ouverte" else "Fermée"
                        VelociteSortField.CONNECTED -> if (statusPositive) "Connectée" else "Hors-ligne"
                        else -> ""
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = valueColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = valueColor
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

            StopActionsContainer(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false },
                stopName = cleanName,
                stopId = station.number.toString(),
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onFillQuery = onFillQuery,
                includeIdActions = false
            )
        }
        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
