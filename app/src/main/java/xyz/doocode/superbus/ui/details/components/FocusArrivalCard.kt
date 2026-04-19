package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.ginko.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@Composable
fun FocusArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>,
    ligneId: String = "",
    startIndex: Int = 0,
    onStartIndexChanged: (Int) -> Unit = {}
) {
    val lineColor = StopDetailsUtils.resolveHighlightLineColor(
        couleurFond = couleurFond,
        couleurTexte = couleurTexte,
        ligneId = ligneId
    )
    val gradientColors = StopDetailsUtils.getGradientColors(lineColor)
    var currentStartIndex by rememberSaveable(numLigne, destination, startIndex) {
        mutableStateOf(startIndex.coerceIn(0, times.lastIndex.coerceAtLeast(0)))
    }
    LaunchedEffect(times) {
        currentStartIndex = currentStartIndex.coerceIn(0, times.lastIndex.coerceAtLeast(0))
    }
    val displayedTimes = remember(times, currentStartIndex) {
        times.drop(currentStartIndex.coerceAtLeast(0)).ifEmpty { times }
    }

    // Use BoxWithConstraints to detect landscape/tablet width
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .scrollingGradient(gradientColors, displayedTimes, RectangleShape)
    ) {
        // Stripe
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(10.dp)
                .background(lineColor)
        )

        val isLandscape = maxWidth > 600.dp

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Header (Centered)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    FocusHeader(
                        numLigne = numLigne,
                        destination = destination,
                        couleurFond = couleurFond,
                        couleurTexte = couleurTexte,
                        ligneId = ligneId,
                        isLandscape = true
                    )
                }

                // Separator
                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight(0.6f)
                        .width(1.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )

                // Column 2: Times
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    // We need to constrain height if the content is small, but fill width?
                    // Actually FocusTimesContent uses spacers/weights so it expects to fill some space.
                    // Let's create a Column here to simulate the layout logic or adapt FocusTimesContent
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Add some spacers to center it vertically if needed, or let it distribute
                        // FocusTimesContent uses weights, so let's give it a container with height
                        FocusTimesContent(
                            times = displayedTimes,
                            lineColor = lineColor,
                            modifier = Modifier.fillMaxWidth(),
                            onTimeSelected = { relativeIndex ->
                                val newStartIndex = (currentStartIndex + relativeIndex)
                                    .coerceIn(0, times.lastIndex.coerceAtLeast(0))
                                currentStartIndex = newStartIndex
                                onStartIndexChanged(newStartIndex)
                            }
                        )
                    }
                }
            }
        } else {
            // Portrait Mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                FocusHeader(
                    numLigne = numLigne,
                    destination = destination,
                    couleurFond = couleurFond,
                    couleurTexte = couleurTexte,
                    ligneId = ligneId,
                    modifier = Modifier.padding(top = 24.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                FocusTimesContent(
                    times = displayedTimes,
                    lineColor = lineColor,
                    onTimeSelected = { relativeIndex ->
                        val newStartIndex = (currentStartIndex + relativeIndex)
                            .coerceIn(0, times.lastIndex.coerceAtLeast(0))
                        currentStartIndex = newStartIndex
                        onStartIndexChanged(newStartIndex)
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun FocusHeader(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    ligneId: String = "",
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val badgeScale = if (isLandscape) 2.5f else 1.5f
    val textStyle =
        if (isLandscape) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineMedium

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        LineBadge(
            numLigne = numLigne,
            couleurFond = couleurFond,
            couleurTexte = couleurTexte,
            ligneId = ligneId,
            modifier = Modifier
                .scale(badgeScale)
                .height(50.dp)
                .aspectRatio(1f)
                .padding(8.dp)
        )
        // Add more spacing if scaled up to avoid overlap visually if using scale modifier
        Spacer(modifier = Modifier.height(if (isLandscape) 48.dp else 24.dp))
        Text(
            text = destination,
            style = textStyle,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FocusTimesContent(
    times: List<Temps>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    onTimeSelected: (Int) -> Unit = {}
) {
    val firstTimeStr = times.firstOrNull()?.temps
    val isNotServed = firstTimeStr != null && firstTimeStr.equals("Non desservi", ignoreCase = true)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isNotServed) {
            Box(
                modifier = Modifier.weight(
                    1f,
                    fill = false
                ), // Don't force unnecessary weight if not needed
                contentAlignment = Alignment.Center
            ) {
                ServiceNotServed()
            }
        } else {
            // Top time (Primary)
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

            Spacer(modifier = Modifier.height(32.dp)) // Fixed spacer instead of weight for cleaner layout in both modes? 
            // Or keep flexible spacing. In landscape, vertical space might be tight.
            // Let's use a smaller fixed spacer or flexible weight depending on parent.
            // But to reuse easily, let's keep it simple.

            // Bottom 3 times
            val bottomTimes = times.drop(1).take(3)
            if (bottomTimes.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    bottomTimes.forEachIndexed { index, time ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTimeSelected(index + 1) },
                            contentAlignment = Alignment.Center
                        ) {
                            TimeDisplayExpo(time, lineColor)
                        }
                    }
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
