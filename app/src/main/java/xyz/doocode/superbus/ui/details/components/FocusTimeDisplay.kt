package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.tooling.preview.Preview
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@Composable
fun FocusTimeDisplay(temps: Temps, isPrimary: Boolean, textScale: Float = 1f) {
    val timeStr = temps.temps
    val isRealTime = temps.fiable

    val durationMinutes = StopDetailsUtils.parseDurationMinutes(timeStr, temps.tempsEnSeconde)

    // Urgency Logic
    val effectiveMinutes = durationMinutes ?: 99
    val isUrgent = effectiveMinutes < 2 || timeStr.equals("Proche", ignoreCase = true)
    val isNear = effectiveMinutes < 10

    val defaultColor = MaterialTheme.colorScheme.onSurface

    val animatedColor by StopDetailsUtils.rememberBlinkingColor(
        isUrgent = isUrgent,
        isNear = isNear,
        defaultColor = defaultColor
    )

    // Bold logic
    val fontWeight = if (isUrgent || isPrimary) FontWeight.ExtraBold else FontWeight.Normal

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (durationMinutes != null) {
            val styleNumber =
                if (isPrimary) MaterialTheme.typography.displayLarge.copy(fontSize = 140.sp * textScale) else MaterialTheme.typography.displayMedium.copy(
                    fontSize = 60.sp * textScale
                )
            val styleUnit =
                if (isPrimary) MaterialTheme.typography.headlineMedium.copy(fontSize = MaterialTheme.typography.headlineMedium.fontSize * textScale) else MaterialTheme.typography.titleLarge.copy(
                    fontSize = MaterialTheme.typography.titleLarge.fontSize * textScale
                )

            val (mainText, subText) = if (durationMinutes <= 99) {
                durationMinutes.toString() to "min"
            } else {
                val h = durationMinutes / 60
                ">$h" to if (h > 1) "heures" else "heure"
            }

            Text(
                text = mainText,
                style = styleNumber,
                fontWeight = fontWeight,
                color = animatedColor,
                lineHeight = 20.sp * textScale,
                modifier = if (isPrimary) Modifier.offset(y = 16.dp * textScale) else Modifier
            )
            Text(
                text = subText,
                style = styleUnit,
                fontWeight = FontWeight.Bold,
                color = animatedColor.copy(alpha = 0.8f)
            )
        } else {
            // For time like "12:30" or "Proche"
            val displayStr =
                if (timeStr.equals("Proche", ignoreCase = true)) "Proche" else timeStr.replace(
                    ":",
                    "h"
                )
            val styleText =
                if (isPrimary) MaterialTheme.typography.displayMedium.copy(fontSize = 48.sp * textScale) else MaterialTheme.typography.displaySmall.copy(
                    fontSize = MaterialTheme.typography.displaySmall.fontSize * textScale
                )

            Text(
                text = displayStr,
                style = styleText,
                fontWeight = fontWeight,
                color = animatedColor,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp * textScale))

        if (!isRealTime) {
            Text(
                text = "Théorique",
                style = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * textScale),
                color = MaterialTheme.colorScheme.error
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

@Preview(name = "Focus Time Display - Light", showBackground = true)
@Preview(
    name = "Focus Time Display - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true
)
@Composable
private fun FocusTimeDisplayPreview() {
    SuperBusTheme {
        Surface {
            Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)) {
                FocusTimeDisplay(
                    temps = mockTemps("1 min", true),
                    isPrimary = true
                )
                FocusTimeDisplay(
                    temps = mockTemps("5 min", true),
                    isPrimary = false
                )
                FocusTimeDisplay(
                    temps = mockTemps("21h55", false, tempsEnSeconde = 300),
                    isPrimary = false
                )
            }
        }
    }
}

