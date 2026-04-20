package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.jcdecaux.Station

@Composable
fun VelociteStatusCard(station: Station) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ) {
            Box(
                modifier = Modifier.padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Informations",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            VelociteStatusItem(
                modifier = Modifier.weight(1f),
                icon = if (station.status == "OPEN") Icons.Default.CheckCircle
                else Icons.Default.Cancel,
                color = if (station.status == "OPEN") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                label = if (station.status == "OPEN") "Station ouverte" else "Station fermée",
                positive = station.status == "OPEN"
            )

            VelociteStatusItem(
                modifier = Modifier.weight(1f),
                icon = if (station.connected) Icons.Default.Wifi else Icons.Default.WifiOff,
                color = if (station.connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                label = if (station.connected) "Station en ligne" else "Station déconnectée",
                positive = station.connected
            )

            VelociteStatusItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CreditCard,
                color = if (station.banking) MaterialTheme.colorScheme.primary
                else Color.Gray,
                label = if (station.banking) "Paiement CB possible" else "Paiement CB indisponible",
                positive = station.banking
            )

            VelociteStatusItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.AutoAwesome,
                color = if (station.bonus) Color(0xFFFF9800) else Color.Gray,
                label = if (station.bonus) "Station bonus" else "Station non bonus",
                positive = station.bonus
            )
        }
    }
}

@Composable
fun VelociteStatusItem(
    icon: ImageVector,
    color: Color,
    label: String,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    val strikeColor = color
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .size(28.dp)
                    .then(
                        if (!positive) Modifier.drawWithContent {
                            drawContent()
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = strikeColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, 0f),
                                strokeWidth = strokeWidth,
                                cap = StrokeCap.Round
                            )
                        } else Modifier
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}
