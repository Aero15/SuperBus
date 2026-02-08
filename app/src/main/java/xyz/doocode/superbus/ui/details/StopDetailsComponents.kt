package xyz.doocode.superbus.ui.details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import androidx.core.graphics.toColorInt

@Composable
fun ArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>
) {
    // 1. Parse Line Color
    val lineColor = try {
        Color("#$couleurFond".toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    // 2. Mix 10% Line Color with 90% Surface Color
    val cardBackgroundColor =
        lineColor.copy(alpha = 0.12f).compositeOver(MaterialTheme.colorScheme.surface)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
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
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = destination,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Row: Equally distributed space with separators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // Essential for vertical divider
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayTimes = times.take(3)

                displayTimes.forEachIndexed { index, temps ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        TimeDisplayMinimal(temps, lineColor, isFirst = index == 0)
                    }

                    if (index < displayTimes.size - 1) {
                        VerticalDivider(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            thickness = 1.dp,
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimeDisplayMinimal(temps: Temps, accentColor: Color, isFirst: Boolean) {
    val isRealTime = temps.fiable

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = temps.temps.replace("min", " min"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (isFirst) FontWeight.Black else FontWeight.Normal,
            color = if (isRealTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.6f
            )
        )
        if (!isRealTime) {
            Text(
                text = "Théorique",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
        }
    }
}
