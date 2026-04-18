package xyz.doocode.superbus.ui.search.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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
    val isDismissing = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    fun dismissSheetAnimated() {
        if (isDismissing.value) return
        isDismissing.value = true

        scope.launch {
            delay(120)
            sheetState.hide()
            onDismissRequest()
            isDismissing.value = false
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
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Trier les stations",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            //HorizontalDivider()

            Text(
                text = "Sens du tri",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SortOrderTile(
                    label = "Croissant",
                    sortField = currentSortField,
                    sortOrder = VelociteSortOrder.ASCENDING,
                    isSelected = currentSortOrder == VelociteSortOrder.ASCENDING,
                    modifier = Modifier.weight(1f),
                    onClick = { selectOrder(VelociteSortOrder.ASCENDING) }
                )
                SortOrderTile(
                    label = "Décroissant",
                    sortField = currentSortField,
                    sortOrder = VelociteSortOrder.DESCENDING,
                    isSelected = currentSortOrder == VelociteSortOrder.DESCENDING,
                    modifier = Modifier.weight(1f),
                    onClick = { selectOrder(VelociteSortOrder.DESCENDING) }
                )
            }

            //HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

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

            val topSortFieldOptions = listOf(
                //Triple("Nom", VelociteSortField.NAME, Icons.Default.TextFields),
                Triple("Numéro de station", VelociteSortField.NUMBER, Icons.Default.Tag)
            )

            Text(
                text = "Décompte",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            topSortFieldOptions.forEach { (label, field, icon) ->
                SortOptionRow(
                    label = label,
                    icon = icon,
                    isSelected = currentSortField == field,
                    onClick = { selectField(field) }
                )
            }

            val countSortFieldOptions = listOf(
                /*Triple(
                    "Vélos disponibles",
                    VelociteSortField.TOTAL_BIKES,
                    Icons.AutoMirrored.Filled.DirectionsBike
                ),*/
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
                /*Triple(
                    "Places disponibles",
                    VelociteSortField.AVAILABLE_STANDS,
                    Icons.Filled.LocalParking
                ),*/
                Triple(
                    "Bornes indisponibles",
                    VelociteSortField.UNAVAILABLE_STANDS,
                    Icons.Filled.Block
                ),
                //Triple("Capacité", VelociteSortField.CAPACITY, Icons.Filled.Storage),
            )

            countSortFieldOptions.forEach { (label, field, icon) ->
                SortOptionRow(
                    label = label,
                    icon = icon,
                    isSelected = currentSortField == field,
                    onClick = { selectField(field) }
                )
            }

            //HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Text(
                text = "Statut de la station",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val statusSortOptions = listOf(
                Triple("Bonus", VelociteSortField.BONUS, Icons.Default.AutoAwesome),
                Triple("Paiement CB", VelociteSortField.BANKING, Icons.Default.CreditCard),
                Triple("Ouverte", VelociteSortField.OPEN, Icons.Default.CheckCircle),
                Triple("En ligne", VelociteSortField.CONNECTED, Icons.Default.Wifi)
            )

            statusSortOptions.forEach { (label, field, icon) ->
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
private fun SortOrderTile(
    label: String,
    sortField: VelociteSortField,
    sortOrder: VelociteSortOrder,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val containerColor =
        if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }
    val borderColor =
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = containerColor
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(80.dp) // 120.dp
                    .border(1.5.dp, borderColor, RoundedCornerShape(18.dp))
                    .clickable { onClick() }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SortOrderIllustration(
                sortField = sortField,
                sortOrder = sortOrder,
                contentColor = contentColor
            )
            /*Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )*/
        }
    }
}

@Composable
private fun SortOrderIllustration(
    sortField: VelociteSortField,
    sortOrder: VelociteSortOrder,
    contentColor: Color
) {
    val isStatusField = sortField in setOf(
        VelociteSortField.BONUS,
        VelociteSortField.BANKING,
        VelociteSortField.OPEN,
        VelociteSortField.CONNECTED
    )

    if (isStatusField) {
        val firstIcon = when (sortField) {
            VelociteSortField.BONUS -> Icons.Default.AutoAwesome
            VelociteSortField.BANKING -> Icons.Default.CreditCard
            VelociteSortField.OPEN -> Icons.Default.CheckCircle
            VelociteSortField.CONNECTED -> Icons.Default.Wifi
            else -> Icons.Default.Tag
        }
        val positiveFirst = sortOrder == VelociteSortOrder.DESCENDING

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (positiveFirst) firstIcon else Icons.Default.Block,
                contentDescription = null,
                tint = if (positiveFirst) contentColor else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(18.dp)
            )
            Icon(
                imageVector = if (positiveFirst) Icons.Default.Block else firstIcon,
                contentDescription = null,
                tint = if (positiveFirst) MaterialTheme.colorScheme.error else contentColor,
                modifier = Modifier.size(28.dp)
            )
        }
    } else {
        val isNameSort = sortField == VelociteSortField.NAME
        val isAscending = sortOrder == VelociteSortOrder.ASCENDING
        val values =
            if (isNameSort) {
                if (isAscending) listOf("A", "B", "C") else listOf("C", "B", "A")
            } else {
                if (isAscending) listOf("0", "1", "3") else listOf("3", "1", "0")
            }
        val baseColors = listOf(Color(0xFFE53935), Color(0xFFFBC02D), Color(0xFF43A047))
        val baseSizes = listOf(18.sp, 22.sp, 26.sp)
        val colors = if (isAscending) baseColors else baseColors.reversed()
        val sizes = if (isAscending) baseSizes else baseSizes.reversed()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                values.forEachIndexed { index, value ->
                    Text(
                        text = value,
                        color = colors[index],
                        fontSize = sizes[index],
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(18.dp)
            )
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
