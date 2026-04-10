package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.theme.AvailableStandsColor
import xyz.doocode.superbus.ui.theme.ElectricBikeColor
import xyz.doocode.superbus.ui.theme.MechanicalBikeColor

@Composable
fun VelociteRecap(station: Station) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Mechanical bikes tile
        VelociteRecapTile(
            icon = Icons.Filled.PedalBike,
            label = "Vélos\nmécanique",
            value = station.totalStands.availabilities.mechanicalBikes.toString(),
            backgroundColor = MechanicalBikeColor,
            modifier = Modifier.weight(1f)
        )

        // Electric bikes tile
        VelociteRecapTile(
            icon = Icons.Filled.ElectricBike,
            label = "Vélos\nélectrique",
            value = station.totalStands.availabilities.electricalBikes.toString(),
            backgroundColor = ElectricBikeColor,
            modifier = Modifier.weight(1f)
        )

        // Available stands tile
        VelociteRecapTile(
            icon = Icons.Filled.LocalParking,
            label = "Places\ndispo",
            value = station.totalStands.availabilities.stands.toString(),
            backgroundColor = AvailableStandsColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VelociteRecapTile(
    icon: ImageVector,
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val isZero = value.toIntOrNull() == 0

    // Définition des couleurs pour un rendu "Sublime"
    val displayContentColor = if (isZero) MaterialTheme.colorScheme.error else Color.White
    val baseColor = if (isZero) MaterialTheme.colorScheme.surfaceVariant else backgroundColor

    // Création d'un dégradé pour la profondeur
    val backgroundBrush = if (isZero) {
        Brush.linearGradient(
            colors = listOf(baseColor.copy(alpha = 0.5f), baseColor.copy(alpha = 0.3f))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                baseColor, baseColor.copy(
                    alpha = 0.75f
                ) // Légère variation pour l'effet de lumière
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    Card(
        modifier = modifier.height(115.dp), // Hauteur augmentée pour le style carte
        shape = RoundedCornerShape(16.dp), // Coins très arrondis
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            // Icône en filigrane (Watermark) décoratif
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = displayContentColor.copy(alpha = if (isZero) 0.1f else 0.2f),
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 24.dp, y = -24.dp) // Débordement intentionnel
                    .rotate(-15f)
            )

            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Valeur (Chiffre) - Mise en avant majeure
                Text(
                    text = value, style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ), color = displayContentColor
                )

                // Libellé
                Text(
                    text = label.uppercase(), // Majuscules pour un look plus technique/propre
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp
                    ), color = displayContentColor.copy(alpha = 0.9f), lineHeight = 12.sp
                )
            }
        }
    }
}
