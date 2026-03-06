package xyz.doocode.superbus.ui.details

import android.content.res.Configuration
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NoTransfer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@Composable
fun ArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>,
    initialExpoMode: Boolean = false
) {
    val lineColor = parseLineColor(couleurFond)
    val gradientColors = getGradientColors(lineColor)
    val shape = RoundedCornerShape(14.dp)
    var isExpoMode by remember { mutableStateOf(initialExpoMode) }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(colors = gradientColors),
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
                ArrivalCardHeader(
                    numLigne = numLigne,
                    destination = destination,
                    couleurFond = couleurFond,
                    couleurTexte = couleurTexte
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Check for "Non desservi" case
                val firstTime = times.firstOrNull()?.temps
                if (firstTime != null && firstTime.equals("Non desservi", ignoreCase = true)) {
                    ServiceNotServed()
                } else {
                    ArrivalTimesRow(
                        times = times,
                        lineColor = lineColor,
                        isExpoMode = isExpoMode,
                        onToggleMode = { isExpoMode = !isExpoMode }
                    )
                }
            }
        }
    }
}

@Preview(name = "ArrivalCard - Light Mode", showBackground = true)
@Preview(
    name = "ArrivalCard - Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ArrivalCardPreview() {
    SuperBusTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ArrivalCard(
                    numLigne = "L4",
                    destination = "Chateaufarine",
                    couleurFond = "FF0000",
                    couleurTexte = "FFFFFF",
                    times = listOf(
                        mockTemps("Non desservi", true)
                    )
                )
                ArrivalCard(
                    numLigne = "L5",
                    destination = "Saint-Claude",
                    couleurFond = "128225",
                    couleurTexte = "FFFFFF",
                    times = listOf(
                        mockTemps("Proche", true),
                        mockTemps("5 min", true),
                        mockTemps("12 min", false)
                    )
                )
            }
        }
    }
}

@Composable
fun ArrivalCardHeader(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String
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
}

@Preview(name = "Header - Light Mode", showBackground = true)
@Preview(
    name = "Header - Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ArrivalCardHeaderPreview() {
    SuperBusTheme {
        Surface {
            ArrivalCardHeader(
                numLigne = "L3",
                destination = "Pole Temis",
                couleurFond = "00558f",
                couleurTexte = "FFFFFF"
            )
        }
    }
}

@Composable
fun ServiceNotServed() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.NoTransfer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Non desservi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(name = "No Service - Light Mode", showBackground = true)
@Preview(
    name = "No Service - Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ServiceNotServedPreview() {
    SuperBusTheme {
        Surface {
            ServiceNotServed()
        }
    }
}

@Composable
fun ArrivalTimesRow(
    times: List<Temps>,
    lineColor: Color,
    isExpoMode: Boolean = false,
    onToggleMode: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable { onToggleMode() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val displayTimes = times.take(3)

        displayTimes.forEachIndexed { index, temps ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isExpoMode) {
                    TimeDisplayExpo(temps, lineColor)
                } else {
                    TimeDisplayMinimal(temps, lineColor, isFirst = index == 0)
                }
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

@Composable
fun TimeDisplayExpo(temps: Temps, accentColor: Color) {
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
        defaultColor.copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)
    }

    // Blink Animation
    val infiniteTransition = rememberInfiniteTransition(label = "blink_expo")
    val animatedColor by if (isUrgent || isNear) {
        val duration = if (isUrgent) 600 else 900
        infiniteTransition.animateColor(
            initialValue = defaultColor,
            targetValue = blinkTargetColor,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "color_expo"
        )
    } else {
        rememberUpdatedState(defaultColor)
    }

    val numberOnly = if (timeStr.contains("min")) {
        timeStr.filter { it.isDigit() }
    } else null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!numberOnly.isNullOrEmpty()) {
            // Display for minutes
            Text(
                text = numberOnly,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = if (isUrgent || isNear) FontWeight.Black else FontWeight.Normal
                ),
                color = animatedColor,
                lineHeight = 40.sp,
                modifier = Modifier.offset(y = 4.dp)
            )
            Text(
                text = "min",
                style = MaterialTheme.typography.bodyMedium,
                color = animatedColor,
                modifier = Modifier.offset(y = (-4).dp)
            )
        } else {
            // Display for time like "20:45" or "Proche"
            Text(
                text = timeStr,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }

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

@Preview(name = "Times Row - Light Mode", showBackground = true)
@Preview(
    name = "Times Row - Dark Mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun ArrivalTimesRowPreview() {
    SuperBusTheme {
        Surface {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Normal Mode:", modifier = Modifier.padding(4.dp))
                ArrivalTimesRow(
                    times = listOf(
                        mockTemps("2 min", true),
                        mockTemps("8 min", false),
                        mockTemps("15 min", true)
                    ),
                    lineColor = Color.Red,
                    isExpoMode = false
                )

                HorizontalDivider()

                Text("Expo Mode:", modifier = Modifier.padding(4.dp))
                ArrivalTimesRow(
                    times = listOf(
                        mockTemps("2 min", true),
                        mockTemps("8 min", false),
                        mockTemps("20:40", true)
                    ),
                    lineColor = Color.Red,
                    isExpoMode = true
                )
            }
        }
    }
}

@Preview(name = "Time Display - Light", showBackground = true)
@Preview(
    name = "Time Display - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun TimeDisplayPreview() {
    SuperBusTheme {
        Surface {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TimeDisplayMinimal(
                    temps = mockTemps("1 min", true),
                    accentColor = Color.Red,
                    isFirst = true
                )
                TimeDisplayMinimal(
                    temps = mockTemps("5 min", true),
                    accentColor = MaterialTheme.colorScheme.primary,
                    isFirst = false
                )
                TimeDisplayMinimal(
                    temps = mockTemps("12 min", false),
                    accentColor = MaterialTheme.colorScheme.primary,
                    isFirst = false
                )
            }
        }
    }
}

private fun mockTemps(
    temps: String = "5 min",
    fiable: Boolean = true
): Temps {
    return Temps(
        idArret = "", latitude = 0.0, longitude = 0.0, idLigne = "", numLignePublic = "",
        couleurFond = "", couleurTexte = "", sensAller = true, destination = "",
        precisionDestination = "", temps = temps, tempsHTML = "", tempsEnSeconde = 0,
        typeDeTemps = 0, alternance = false, tempsHTMLEnAlternance = "", fiable = fiable,
        numVehicule = "", accessibiliteArret = 0, accessibiliteVehicule = 0, affluence = 0,
        texteAffluence = "", aideDecisionAffluence = "", tauxDeCharge = 0.0, idInfoTrafic = 0,
        modeTransport = 0
    )
}

@Composable
private fun parseLineColor(couleurFond: String): Color {
    return try {
        Color("#$couleurFond".toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun getGradientColors(lineColor: Color): List<Color> {
    val startColor = lineColor.copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)
    val endColor = lineColor.copy(alpha = 0.1f).compositeOver(MaterialTheme.colorScheme.surface)
    return listOf(startColor, endColor)
}