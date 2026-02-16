package xyz.doocode.superbus.ui.details

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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

    // 2. Gradient Colors
    val startColor = lineColor.copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)
    val endColor = lineColor.copy(alpha = 0.1f).compositeOver(MaterialTheme.colorScheme.surface)
    val shape = RoundedCornerShape(14.dp)

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent, // Transparent to show gradient
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                ),
                shape = shape
            )
    ) {
        Column {
            // Top Border Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(lineColor)
            )
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
}

@Composable
fun TimeDisplayMinimal(temps: Temps, accentColor: Color, isFirst: Boolean) {
    val isRealTime = temps.fiable
    val timeStr = temps.temps
    // Extract minutes from string like "5 min"
    val minutes = if (timeStr.contains("min")) {
        timeStr.filter { it.isDigit() }.toIntOrNull()
    } else 99 // Assume far if parsing fails or formatted as HH:MM

    val isUrgent = minutes != null && minutes < 2
    val isNear = minutes != null && minutes < 10

    // Colors
    val defaultColor =
        if (isRealTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
    val blinkTargetColor = if (isUrgent) {
        Color.Red
    } else {
        // Lighter solid color (simulating 30% alpha over surface)
        defaultColor.copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)
    }

    // Blink Animation
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val animatedColor by if (isUrgent || isNear) {
        val duration = if (isUrgent) 600 else 900
        infiniteTransition.animateColor(
            initialValue = defaultColor,
            targetValue = blinkTargetColor,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "color"
        )
    } else {
        rememberUpdatedState(defaultColor)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = temps.temps.replace("min", " min"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = if (isFirst) FontWeight.Black else FontWeight.Normal,
            color = animatedColor
        )
        if (isUrgent) {
            Text(
                text = "Imminent",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp
            )
        } else if (!isRealTime) {
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
