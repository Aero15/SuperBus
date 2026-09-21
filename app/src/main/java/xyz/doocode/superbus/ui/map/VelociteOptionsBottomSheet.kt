package xyz.doocode.superbus.ui.map

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteOptionsBottomSheet(
    selectedMode: VelociteMapDisplayMode,
    onModeSelected: (VelociteMapDisplayMode) -> Unit,
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

    fun selectMode(mode: VelociteMapDisplayMode) {
        onModeSelected(mode)
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
                text = "Calque Vélocité",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Text(
                text = "Options rapides",
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
                FeaturedOptionBubble(
                    label = "Basique",
                    icon = Icons.Default.Info,
                    isSelected = selectedMode == VelociteMapDisplayMode.BASIC,
                    onClick = { selectMode(VelociteMapDisplayMode.BASIC) }
                )
                FeaturedOptionBubble(
                    label = "Vélos",
                    icon = Icons.AutoMirrored.Filled.DirectionsBike,
                    isSelected = selectedMode == VelociteMapDisplayMode.AVAILABLE_BIKES,
                    onClick = { selectMode(VelociteMapDisplayMode.AVAILABLE_BIKES) }
                )
                FeaturedOptionBubble(
                    label = "Places",
                    icon = Icons.Filled.LocalParking,
                    isSelected = selectedMode == VelociteMapDisplayMode.AVAILABLE_STANDS,
                    onClick = { selectMode(VelociteMapDisplayMode.AVAILABLE_STANDS) }
                )
                FeaturedOptionBubble(
                    label = "Capacité",
                    icon = Icons.Filled.Storage,
                    isSelected = selectedMode == VelociteMapDisplayMode.CAPACITY,
                    onClick = { selectMode(VelociteMapDisplayMode.CAPACITY) }
                )
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "Décompte",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val countOptions = listOf(
                Triple(
                    "Vélos mécaniques",
                    VelociteMapDisplayMode.MECHANICAL_BIKES,
                    Icons.Filled.PedalBike
                ),
                Triple(
                    "Vélos électriques",
                    VelociteMapDisplayMode.ELECTRICAL_BIKES,
                    Icons.Filled.ElectricBike
                ),
                Triple("Numéro de station", VelociteMapDisplayMode.NUMBER, Icons.Filled.Tag),
                Triple(
                    "Bornes indisponibles",
                    VelociteMapDisplayMode.UNAVAILABLE_STANDS,
                    Icons.Filled.Block
                )
            )

            countOptions.forEach { (label, mode, icon) ->
                VelociteOptionRow(
                    label = label,
                    icon = icon,
                    isSelected = selectedMode == mode,
                    onClick = { selectMode(mode) }
                )
            }

            Spacer(modifier = Modifier.padding(top = 16.dp))

            Text(
                text = "Etat de la station",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            val statusOptions = listOf(
                Triple("Ouverte", VelociteMapDisplayMode.OPEN, Icons.Filled.CheckCircle),
                Triple("En ligne", VelociteMapDisplayMode.CONNECTED, Icons.Filled.Wifi),
                Triple("Bonus", VelociteMapDisplayMode.BONUS, Icons.Filled.AutoAwesome),
                Triple("Paiement CB", VelociteMapDisplayMode.BANKING, Icons.Filled.CreditCard)
            )

            statusOptions.forEach { (label, mode, icon) ->
                VelociteOptionRow(
                    label = label,
                    icon = icon,
                    isSelected = selectedMode == mode,
                    onClick = { selectMode(mode) }
                )
            }
        }
    }
}

@Composable
private fun FeaturedOptionBubble(
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
private fun VelociteOptionRow(
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
