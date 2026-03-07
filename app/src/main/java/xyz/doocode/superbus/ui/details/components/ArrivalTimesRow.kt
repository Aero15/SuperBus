package xyz.doocode.superbus.ui.details.components

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArrivalTimesRow(
    times: List<Temps>,
    lineColor: Color,
    isExpoMode: Boolean = false,
    onToggleMode: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .combinedClickable(
                onClick = onToggleMode,
                onLongClick = onLongClick
            ),
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

                androidx.compose.material3.HorizontalDivider()

                Text("Expo Mode:", modifier = Modifier.padding(4.dp))
                ArrivalTimesRow(
                    times = listOf(
                        mockTemps("2 min", true),
                        mockTemps("8 min", false),
                        mockTemps("20:40", true, 2400),
                        mockTemps("21:15", false, 4500)
                    ),
                    lineColor = Color.Red,
                    isExpoMode = true
                )
            }
        }
    }
}
