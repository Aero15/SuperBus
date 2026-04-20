package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.ginko.Temps
import xyz.doocode.superbus.core.dto.ginko.VehiculeDR
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
    val context = LocalContext.current
    val referenceDataRepository = remember(context) { ReferenceDataRepository.getInstance(context) }

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
    val updateStartIndex: (Int) -> Unit = { newIndex ->
        val boundedIndex = newIndex.coerceIn(0, times.lastIndex.coerceAtLeast(0))
        currentStartIndex = boundedIndex
        onStartIndexChanged(boundedIndex)
    }
    val targetedDeparture = times.getOrNull(currentStartIndex)
    val targetedVehicleNumber = targetedDeparture?.numVehicule?.trim().orEmpty()
    var vehicleInfo by remember(targetedVehicleNumber) { mutableStateOf<VehiculeDR?>(null) }
    var isVehicleInfoLoading by remember(targetedVehicleNumber) { mutableStateOf(false) }

    LaunchedEffect(targetedVehicleNumber) {
        if (targetedVehicleNumber.isBlank()) {
            vehicleInfo = null
            isVehicleInfoLoading = false
        } else {
            isVehicleInfoLoading = true
            vehicleInfo = referenceDataRepository.getDetailsVehiculeDR(targetedVehicleNumber)
            isVehicleInfoLoading = false
        }
    }

    // Use BoxWithConstraints to detect landscape/tablet width
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > 600.dp

        if (isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scrollingGradient(gradientColors, displayedTimes, RectangleShape)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(lineColor)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    VerticalDivider(
                        modifier = Modifier
                            .fillMaxHeight(0.6f)
                            .width(1.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            if (currentStartIndex == 0) {
                                FocusTimesContent(
                                    times = displayedTimes,
                                    lineColor = lineColor,
                                    modifier = Modifier.fillMaxWidth(),
                                    onTimeSelected = { relativeIndex ->
                                        updateStartIndex(currentStartIndex + relativeIndex)
                                    }
                                )
                            } else {
                                FocusShiftedTimesContent(
                                    times = times,
                                    currentIndex = currentStartIndex,
                                    lineColor = lineColor,
                                    modifier = Modifier.fillMaxWidth(),
                                    onPreviousClick = { updateStartIndex(currentStartIndex - 1) },
                                    onNextClick = { updateStartIndex(currentStartIndex + 1) }
                                )
                            }

                            VehicleInfoCard(
                                vehicleNumber = targetedVehicleNumber,
                                vehicle = vehicleInfo,
                                isLoading = isVehicleInfoLoading
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scrollingGradient(gradientColors, displayedTimes, RectangleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(10.dp)
                            .background(lineColor)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        FocusHeader(
                            numLigne = numLigne,
                            destination = destination,
                            couleurFond = couleurFond,
                            couleurTexte = couleurTexte,
                            ligneId = ligneId,
                            modifier = Modifier.padding(top = 24.dp)
                        )

                        if (currentStartIndex == 0) {
                            FocusTimesContent(
                                times = displayedTimes,
                                lineColor = lineColor,
                                onTimeSelected = { relativeIndex ->
                                    updateStartIndex(currentStartIndex + relativeIndex)
                                }
                            )
                        } else {
                            FocusShiftedTimesContentPortrait(
                                times = times,
                                currentIndex = currentStartIndex,
                                lineColor = lineColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                onPreviousClick = { updateStartIndex(currentStartIndex - 1) },
                                onNextClick = { updateStartIndex(currentStartIndex + 1) }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                VehicleInfoCard(
                    vehicleNumber = targetedVehicleNumber,
                    vehicle = vehicleInfo,
                    isLoading = isVehicleInfoLoading,
                    modifier = Modifier.fillMaxWidth(),
                    fullBleed = true
                )

                Spacer(modifier = Modifier.height(16.dp))
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
        Spacer(modifier = Modifier.height(if (isLandscape) 48.dp else 12.dp))
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

@Composable
private fun FocusShiftedTimesContent(
    times: List<Temps>,
    currentIndex: Int,
    lineColor: Color,
    modifier: Modifier = Modifier,
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val previousTime = times.getOrNull(currentIndex - 1)
    val selectedTime = times.getOrNull(currentIndex)
    val nextTime = times.getOrNull(currentIndex + 1)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FocusShiftedSideTime(
            temps = previousTime,
            lineColor = lineColor,
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Départ précédent"
                )
            },
            modifier = Modifier.weight(1f),
            onClick = onPreviousClick
        )

        Box(
            modifier = Modifier.weight(1.2f),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTime != null) {
                FocusTimeDisplay(selectedTime, isPrimary = true)
            }
        }

        if (nextTime != null) {
            FocusShiftedSideTime(
                temps = nextTime,
                lineColor = lineColor,
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Départ suivant"
                    )
                },
                modifier = Modifier.weight(1f),
                onClick = onNextClick
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun FocusShiftedTimesContentPortrait(
    times: List<Temps>,
    currentIndex: Int,
    lineColor: Color,
    modifier: Modifier = Modifier,
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    val previousTime = times.getOrNull(currentIndex - 1)
    val selectedTime = times.getOrNull(currentIndex)
    val nextTime = times.getOrNull(currentIndex + 1)

    Box(
        modifier = modifier.heightIn(min = 200.dp)
    ) {
        if (previousTime != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-12).dp, y = (-4).dp)
                    .width(82.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(0.78f)
                        .clickable { onPreviousClick() },
                    contentAlignment = Alignment.Center
                ) {
                    TimeDisplayExpo(previousTime, lineColor)
                }
            }

            FilledTonalIconButton(
                onClick = onPreviousClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 10.dp, y = 54.dp)
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Départ précédent"
                )
            }
        }

        if (nextTime != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 12.dp, y = (-4).dp)
                    .width(82.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(0.78f)
                        .clickable { onNextClick() },
                    contentAlignment = Alignment.Center
                ) {
                    TimeDisplayExpo(nextTime, lineColor)
                }
            }

            FilledTonalIconButton(
                onClick = onNextClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-10).dp, y = 54.dp)
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Départ suivant"
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTime != null) {
                FocusTimeDisplay(selectedTime, isPrimary = true)
            }
        }
    }
}

@Composable
private fun FocusShiftedSideTime(
    temps: Temps?,
    lineColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .heightIn(min = 140.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .then(if (temps != null) Modifier.clickable { onClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (temps != null) {
                TimeDisplayExpo(temps, lineColor)
            } else {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (temps != null) {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(40.dp)
            ) {
                icon()
            }
        } else {
            Spacer(modifier = Modifier.size(40.dp))
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
