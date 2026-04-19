package xyz.doocode.superbus.ui.details.components

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import kotlinx.coroutines.launch
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.ginko.Ligne
import xyz.doocode.superbus.core.dto.ginko.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.linedetails.LineDetailsActivity
import xyz.doocode.superbus.ui.lines.LineVariantsSheetContent
import xyz.doocode.superbus.ui.theme.SuperBusTheme

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val referenceDataRepository = remember(context) { ReferenceDataRepository.getInstance(context) }

    val lineColor = StopDetailsUtils.resolveHighlightLineColor(
        couleurFond = couleurFond,
        couleurTexte = couleurTexte,
        ligneId = ligneId
    )
    val gradientColors = StopDetailsUtils.getGradientColors(lineColor)
    val shape = RoundedCornerShape(14.dp)
    var isWaitingTimesLarge by remember { mutableStateOf(initialExpoMode) }
    var isExpanded by remember { mutableStateOf(true) }
    var selectedLine by remember { mutableStateOf<Ligne?>(null) }
    val lineSheetState = rememberModalBottomSheetState()
    val contentPadding by animateDpAsState(
        targetValue = if (isExpanded) 10.dp else 0.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "arrivalCardContentPadding"
    )

    LaunchedEffect(forcedExpandState) {
        if (forcedExpandState != null) {
            isExpanded = forcedExpandState
        }
    }

    if (selectedLine != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedLine = null },
            sheetState = lineSheetState
        ) {
            LineVariantsSheetContent(
                line = selectedLine!!,
                onVariantClick = { variant ->
                    val line = selectedLine!!
                    val intent = Intent(context, LineDetailsActivity::class.java).apply {
                        putExtra("EXTRA_LINE_ID", line.id)
                        putExtra("EXTRA_VARIANT_ID", variant.id)
                        putExtra("EXTRA_LINE_JSON", Gson().toJson(line))
                    }
                    context.startActivity(intent)
                    selectedLine = null
                }
            )
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
                onClick = {},
                onLongClick = onLongClick
            )
            .scrollingGradient(
                colors = gradientColors,
                trigger = times,
                shape = shape,
                isPersistent = isExpanded
            )
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
                modifier = Modifier.padding(contentPadding)
            ) {
                ArrivalCardHeader(
                    numLigne = numLigne,
                    destination = destination,
                    couleurFond = couleurFond,
                    couleurTexte = couleurTexte,
                    ligneId = ligneId,
                    isExpanded = isExpanded,
                    onToggleExpand = { isExpanded = !isExpanded },
                    onOpenLineDetails = {
                        scope.launch {
                            selectedLine = referenceDataRepository.getLignes().firstOrNull {
                                it.id == ligneId || it.numLignePublic.equals(
                                    numLigne,
                                    ignoreCase = true
                                )
                            }
                        }
                    },
                    onLongClick = onLongClick
                )

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                    isExpoMode = isWaitingTimesLarge,
                                    onToggleMode = { isWaitingTimesLarge = !isWaitingTimesLarge },
                                    onLongClick = onLongClick
                                )
                            }
                        }

                        FilledTonalIconButton(
                            onClick = { isWaitingTimesLarge = !isWaitingTimesLarge },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isWaitingTimesLarge) Icons.Default.TextDecrease else Icons.Default.TextIncrease,
                                contentDescription = if (isWaitingTimesLarge) "Réduire la police" else "Agrandir la police"
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
    onOpenLineDetails: () -> Unit = {},
    onLongClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = onOpenLineDetails,
                    onLongClick = onLongClick
                )
                .padding(vertical = 2.dp)
        ) {
            LineBadge(
                numLigne = numLigne,
                couleurFond = couleurFond,
                couleurTexte = couleurTexte,
                ligneId = ligneId,
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = destination,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(onClick = onToggleExpand) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Réduire la carte" else "Agrandir la carte"
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
