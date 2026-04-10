package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.theme.AvailableStandsYellow
import xyz.doocode.superbus.ui.theme.ElectricBikeGreen

@Composable
fun VelociteStatusCard(station: Station) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Informations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VelociteStatusItem(
                    icon = if (station.status == "OPEN") Icons.Default.CheckCircle
                    else Icons.Default.Cancel,
                    color = if (station.status == "OPEN") ElectricBikeGreen else Color.Red,
                    label = if (station.status == "OPEN") "Ouverte" else "Fermée"
                )

                VelociteStatusItem(
                    icon = if (station.connected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    color = if (station.connected) MaterialTheme.colorScheme.primary
                    else Color.Gray,
                    label = if (station.connected) "En Ligne" else "Déconnectée"
                )

                VelociteStatusItem(
                    icon = Icons.Default.CreditCard,
                    color = if (station.banking) MaterialTheme.colorScheme.primary
                    else Color.Gray,
                    label = "CB",
                    strikeThrough = !station.banking
                )

                VelociteStatusItem(
                    icon = Icons.Default.Star,
                    color = if (station.bonus) AvailableStandsYellow else Color.Gray,
                    label = "Bonus",
                    strikeThrough = !station.bonus
                )
            }
        }
    }
}

@Composable
fun VelociteStatusItem(
    icon: ImageVector, color: Color, label: String, strikeThrough: Boolean = false
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            textDecoration = if (strikeThrough) TextDecoration.LineThrough
            else null
        )
    }
}
