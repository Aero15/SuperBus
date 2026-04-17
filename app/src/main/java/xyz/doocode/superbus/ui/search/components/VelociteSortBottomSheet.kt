package xyz.doocode.superbus.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xyz.doocode.superbus.ui.search.VelociteSortField
import xyz.doocode.superbus.ui.search.VelociteSortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteSortBottomSheet(
    currentSortField: VelociteSortField,
    currentSortOrder: VelociteSortOrder,
    onSortFieldChange: (VelociteSortField) -> Unit,
    onSortOrderChange: (VelociteSortOrder) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    fun dismissSheetAnimated() {
        scope.launch {
            sheetState.hide()
            onDismissRequest()
        }
    }

    fun selectOrder(order: VelociteSortOrder) {
        onSortOrderChange(order)
        dismissSheetAnimated()
    }

    fun selectField(field: VelociteSortField) {
        onSortFieldChange(field)
        dismissSheetAnimated()
    }

    ModalBottomSheet(
        onDismissRequest = { dismissSheetAnimated() },
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = "Tri des stations Vélocité",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider()

            Text(
                text = "Ordre de tri",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SortOrderCard(
                    label = "Croissant",
                    icon = Icons.Filled.NorthEast,
                    isSelected = currentSortOrder == VelociteSortOrder.ASCENDING,
                    modifier = Modifier.weight(1f),
                    onClick = { selectOrder(VelociteSortOrder.ASCENDING) }
                )
                SortOrderCard(
                    label = "Décroissant",
                    icon = Icons.Filled.SouthEast,
                    isSelected = currentSortOrder == VelociteSortOrder.DESCENDING,
                    modifier = Modifier.weight(1f),
                    onClick = { selectOrder(VelociteSortOrder.DESCENDING) }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Trier par",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FeaturedSortBubble(
                    label = "Nom",
                    icon = Icons.Default.TextFields,
                    isSelected = currentSortField == VelociteSortField.NAME,
                    onClick = { selectField(VelociteSortField.NAME) }
                )
                FeaturedSortBubble(
                    label = "Vélos",
                    icon = Icons.AutoMirrored.Filled.DirectionsBike,
                    isSelected = currentSortField == VelociteSortField.TOTAL_BIKES,
                    onClick = { selectField(VelociteSortField.TOTAL_BIKES) }
                )
                FeaturedSortBubble(
                    label = "Places",
                    icon = Icons.Filled.LocalParking,
                    isSelected = currentSortField == VelociteSortField.AVAILABLE_STANDS,
                    onClick = { selectField(VelociteSortField.AVAILABLE_STANDS) }
                )
                FeaturedSortBubble(
                    label = "Capacité",
                    icon = Icons.Filled.Storage,
                    isSelected = currentSortField == VelociteSortField.CAPACITY,
                    onClick = { selectField(VelociteSortField.CAPACITY) }
                )
            }

            Spacer(modifier = Modifier.padding(top = 8.dp))

            val sortFieldOptions = listOf(
                Triple("Nom", VelociteSortField.NAME, Icons.Default.TextFields),
                Triple("Numéro", VelociteSortField.NUMBER, Icons.Default.Tag),
                Triple(
                    "Vélos disponibles",
                    VelociteSortField.TOTAL_BIKES,
                    Icons.AutoMirrored.Filled.DirectionsBike
                ),
                Triple(
                    "Vélos mécaniques",
                    VelociteSortField.MECHANICAL_BIKES,
                    Icons.Filled.PedalBike
                ),
                Triple(
                    "Vélos électriques",
                    VelociteSortField.ELECTRICAL_BIKES,
                    Icons.Filled.ElectricBike
                ),
                Triple(
                    "Places disponibles",
                    VelociteSortField.AVAILABLE_STANDS,
                    Icons.Filled.LocalParking
                ),
                Triple(
                    "Bornes indisponibles",
                    VelociteSortField.UNAVAILABLE_STANDS,
                    Icons.Filled.Block
                ),
                Triple("Capacité", VelociteSortField.CAPACITY, Icons.Filled.Storage),
            )

            sortFieldOptions.forEach { (label, field, icon) ->
                SortOptionRow(
                    label = label,
                    icon = icon,
                    isSelected = currentSortField == field,
                    onClick = { selectField(field) }
                )
            }
        }
    }
}

@Composable
private fun SortOrderCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = contentColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FeaturedSortBubble(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bubbleColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val iconColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(bubbleColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
        }
        Text(
            text = label,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SortOptionRow(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Sélectionné",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
