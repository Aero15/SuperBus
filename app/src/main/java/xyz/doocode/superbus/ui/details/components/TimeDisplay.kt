package xyz.doocode.superbus.ui.details.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@Composable
fun TimeDisplayExpo(temps: Temps, accentColor: Color, textScale: Float = 1f) {
    val isRealTime = temps.fiable
    val timeStr = temps.temps

    // Use shared utility for parsing
    val durationMinutes = StopDetailsUtils.parseDurationMinutes(timeStr, temps.tempsEnSeconde)

    val effectiveMinutes = durationMinutes ?: 99
    val isUrgent = effectiveMinutes < 2
    val isNear = effectiveMinutes < 10

    val defaultColor =
        if (isRealTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary

    // Use shared utility for blinking
    val animatedColor by StopDetailsUtils.rememberBlinkingColor(
        isUrgent = isUrgent,
        isNear = isNear,
        defaultColor = defaultColor
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val (mainText, subText) = remember(timeStr, durationMinutes) {
            if (durationMinutes != null) {
                if (durationMinutes <= 99) {
                    durationMinutes.toString() to "min"
                } else {
                    val h = durationMinutes / 60
                    ">$h" to if (h > 1) "heures" else "heure"
                }
            } else if (timeStr.matches(Regex("\\d{1,2}:\\d{2}"))) {
                val parts = timeStr.split(":")
                "${parts[0]}h" to parts[1]
            } else {
                null to null
            }
        }

        if (mainText != null && subText != null) {
            // Display for minutes, hours, or HH:MM
            Text(
                text = mainText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = if (isUrgent) FontWeight.Black else FontWeight.Normal,
                    fontSize = MaterialTheme.typography.displayLarge.fontSize * textScale
                ),
                color = animatedColor,
                lineHeight = 40.sp * textScale,
                modifier = Modifier.offset(y = 4.dp * textScale)
            )
            Text(
                text = subText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * textScale
                ),
                color = animatedColor,
                modifier = Modifier.offset(y = (-4).dp * textScale)
            )
        } else {
            // Display for text like "Proche" or unparseable formats
            Text(
                text = timeStr,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = MaterialTheme.typography.headlineMedium.fontSize * textScale
                ),
                fontWeight = FontWeight.Bold,
                color = animatedColor
            )
        }

        if (!isRealTime) {
            Text(
                text = "Théorique",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp * textScale),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp * textScale
            )
        }
    }
}

@Composable
fun TimeDisplayMinimal(temps: Temps, accentColor: Color, isFirst: Boolean) {
    val isRealTime = temps.fiable
    val timeStr = temps.temps

    // Simple parsing for minimal display, but can benefit from shared logic if consistent
    // Original code: val minutes = if (timeStr.contains("min")) ...
    // Let's use the shared logic for consistency
    val minutes = StopDetailsUtils.parseDurationMinutes(timeStr, temps.tempsEnSeconde) ?: 99

    val isUrgent = minutes < 2
    val isNear = minutes < 10

    val defaultColor =
        if (isRealTime) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary

    val animatedColor by StopDetailsUtils.rememberBlinkingColor(
        isUrgent = isUrgent,
        isNear = isNear,
        defaultColor = defaultColor
    )

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

// Mock Helper for Previews
private fun mockTemps(
    temps: String = "5 min",
    fiable: Boolean = true,
    tempsEnSeconde: Int = 0
): Temps {
    return Temps(
        idArret = "", latitude = 0.0, longitude = 0.0, idLigne = "", numLignePublic = "",
        couleurFond = "", couleurTexte = "", sensAller = true, destination = "",
        precisionDestination = "", temps = temps, tempsHTML = "", tempsEnSeconde = tempsEnSeconde,
        typeDeTemps = 0, alternance = false, tempsHTMLEnAlternance = "", fiable = fiable,
        numVehicule = "", accessibiliteArret = 0, accessibiliteVehicule = 0, affluence = 0,
        texteAffluence = "", aideDecisionAffluence = "", tauxDeCharge = 0.0, idInfoTrafic = 0,
        modeTransport = 0
    )
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
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Minimal:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp
                    )
                ) {
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
                        temps = mockTemps("16 min", true),
                        accentColor = MaterialTheme.colorScheme.primary,
                        isFirst = false
                    )
                }

                Text("Expo:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(
                        16.dp
                    )
                ) {
                    TimeDisplayExpo(
                        temps = mockTemps("1 min", true),
                        accentColor = Color.Red
                    )
                    TimeDisplayExpo(
                        temps = mockTemps("12 min", false),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    TimeDisplayExpo(
                        temps = mockTemps("88 min", false),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

