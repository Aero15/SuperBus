package xyz.doocode.superbus.ui.details.components

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.ginko.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArrivalCard(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    times: List<Temps>,
    ligneId: String = "",
    initialExpoMode: Boolean = false,
    forcedExpandState: Boolean? = null,
    onLongClick: (() -> Unit)? = null
) {
    val lineColor = StopDetailsUtils.resolveHighlightLineColor(
        couleurFond = couleurFond,
        couleurTexte = couleurTexte,
        ligneId = ligneId
    )
    val gradientColors = StopDetailsUtils.getGradientColors(lineColor)
    val shape = RoundedCornerShape(14.dp)
    var isExpoMode by remember { mutableStateOf(initialExpoMode) }
    var isExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(forcedExpandState) {
        if (forcedExpandState != null) {
            isExpanded = forcedExpandState
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {}, // No op click on body (handled by children or ignored)
                onLongClick = onLongClick
            )
            .scrollingGradient(gradientColors, times, shape)
    ) {
        Column {
            // Top Border Strip
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(lineColor)
                )
            }

            Column(
                modifier = Modifier.padding(10.dp)
            ) {
                ArrivalCardHeader(
                    numLigne = numLigne,
                    destination = destination,
                    couleurFond = couleurFond,
                    couleurTexte = couleurTexte,
                    ligneId = ligneId,
                    isExpanded = isExpanded,
                    onToggleExpand = { isExpanded = !isExpanded },
                    onLongClick = onLongClick
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))

                        // Check for "Non desservi" case
                        val firstTime = times.firstOrNull()?.temps
                        if (firstTime != null && firstTime.equals(
                                "Non desservi",
                                ignoreCase = true
                            )
                        ) {
                            ServiceNotServed()
                        } else {
                            ArrivalTimesRow(
                                times = times,
                                lineColor = lineColor,
                                isExpoMode = isExpoMode,
                                onToggleMode = { isExpoMode = !isExpoMode },
                                onLongClick = onLongClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArrivalCardHeader(
    numLigne: String,
    destination: String,
    couleurFond: String,
    couleurTexte: String,
    ligneId: String = "",
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onToggleExpand,
                onLongClick = onLongClick
            )
    ) {
        LineBadge(
            numLigne = numLigne,
            couleurFond = couleurFond,
            couleurTexte = couleurTexte,
            ligneId = ligneId
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = destination,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Réduire" else "Étendre",
            modifier = Modifier.rotate(rotation)
        )
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
                couleurTexte = "FFFFFF",
                isExpanded = true,
                onToggleExpand = {}
            )
        }
    }
}
