package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.LineBadge

@Composable
fun ArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LineBadge(
                    numLigne = numLigne,
                    couleurFond = couleurFond,
                    couleurTexte = couleurTexte
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                times.take(3).forEach { temps ->
                    TimeDisplay(temps)
                }
            }
        }
    }
}

@Composable
fun TimeDisplay(temps: Temps) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val displayTime = temps.temps // "1 min", "15:42", etc.
        val isRealTime = temps.fiable

        Text(
            text = displayTime,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (!isRealTime) {
            Text(
                text = "Théorique",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
