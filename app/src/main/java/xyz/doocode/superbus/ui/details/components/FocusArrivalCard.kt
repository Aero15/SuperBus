package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@Composable
fun FocusArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>
) {
    val lineColor = StopDetailsUtils.parseLineColor(couleurFond)
    val gradientColors = StopDetailsUtils.getGradientColors(lineColor)

    val firstTimeStr = times.firstOrNull()?.temps
    val isNotServed = firstTimeStr != null && firstTimeStr.equals("Non desservi", ignoreCase = true)

    // Use a Box to ensure it fills the parent container completely
    Box(
        modifier = Modifier
            .fillMaxSize()
            .scrollingGradient(gradientColors, times, RectangleShape)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(lineColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Centered, 2 lines, bigger badge
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                ) {
                    LineBadge(
                        numLigne = numLigne,
                        couleurFond = couleurFond,
                        couleurTexte = couleurTexte,
                        modifier = Modifier
                            .scale(1.5f)
                            .height(50.dp)
                            .aspectRatio(1f)
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = destination,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isNotServed) {
                    Box(
                        modifier = Modifier.weight(2f),
                        contentAlignment = Alignment.Center
                    ) {
                        ServiceNotServed()
                    }
                } else {
                    val topTimes = times.take(1)
                    if (topTimes.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            topTimes.forEachIndexed { index, time ->
                                FocusTimeDisplay(time, isPrimary = index == 0)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom 3 times (index 1, 2, 3) because topTimes is take(1)
                    val bottomTimes = times.drop(1).take(3)
                    if (bottomTimes.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            bottomTimes.forEach { time ->
                                // Use existing TimeDisplayExpo but wrap in Box to center
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    TimeDisplayExpo(time, lineColor)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
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

@Preview(name = "Focus Card", showBackground = true, heightDp = 800)
@Composable
private fun FocusArrivalCardPreview() {
    SuperBusTheme {
        FocusArrivalCard(
            numLigne = "L3",
            destination = "Centre Ville",
            couleurFond = "E00000",
            couleurTexte = "FFFFFF",
            times = listOf(
                mockTemps("0 min", true),
                mockTemps("5 min", true),
                mockTemps("12 min", false),
                mockTemps("25 min", false),
                mockTemps("45 min", false)
            )
        )
    }
}
