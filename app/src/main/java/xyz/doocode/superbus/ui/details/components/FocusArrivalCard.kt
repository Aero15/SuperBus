package xyz.doocode.superbus.ui.details.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
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
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.LineBadge
import xyz.doocode.superbus.ui.theme.SuperBusTheme
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.content.edit
import kotlin.math.roundToInt

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

    // Use BoxWithConstraints to detect landscape/tablet width
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .scrollingGradient(gradientColors, times, RectangleShape)
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
                            times = times,
                            lineColor = lineColor,
                            modifier = Modifier.fillMaxWidth()
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
                    modifier = Modifier.padding(top = 24.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                FocusTimesContent(
                    times = times,
                    lineColor = lineColor
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

enum class FontSizeEditMode { None, Primary, Secondary }

@Composable
private fun FocusTimesContent(
    times: List<Temps>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val prefs =
        remember { context.getSharedPreferences("superbus_app_settings", Context.MODE_PRIVATE) }

    var textScalePrimary by remember {
        mutableFloatStateOf(
            prefs.getFloat(
                "focus_primary_text_scale",
                1f
            )
        )
    }
    var textScaleSecondary by remember {
        mutableFloatStateOf(
            prefs.getFloat(
                "focus_secondary_text_scale",
                1f
            )
        )
    }

    val animatedTextScalePrimary by androidx.compose.animation.core.animateFloatAsState(
        targetValue = textScalePrimary,
        animationSpec = androidx.compose.animation.core.tween(300)
    )
    val animatedTextScaleSecondary by androidx.compose.animation.core.animateFloatAsState(
        targetValue = textScaleSecondary,
        animationSpec = androidx.compose.animation.core.tween(300)
    )

    var editingMode by remember { mutableStateOf(FontSizeEditMode.None) }

    fun setPrimary(scale: Float) {
        val s = scale.coerceIn(0.5f, 2.5f)
        textScalePrimary = s
        prefs.edit { putFloat("focus_primary_text_scale", s) }
    }

    fun setSecondary(scale: Float) {
        val s = scale.coerceIn(0.5f, 2.5f)
        textScaleSecondary = s
        prefs.edit { putFloat("focus_secondary_text_scale", s) }
    }

    BackHandler(enabled = editingMode != FontSizeEditMode.None) {
        editingMode = FontSizeEditMode.None
    }

    val firstTimeStr = times.firstOrNull()?.temps
    val isNotServed = firstTimeStr != null && firstTimeStr.equals("Non desservi", ignoreCase = true)

    Column(
        modifier = modifier.pointerInput(editingMode) {
            detectTapGestures(
                onTap = {
                    if (editingMode != FontSizeEditMode.None) {
                        editingMode = FontSizeEditMode.None
                    }
                }
            )
        },
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
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                editingMode = FontSizeEditMode.Primary
                            },
                            onTap = {
                                if (editingMode != FontSizeEditMode.None) editingMode =
                                    FontSizeEditMode.None
                            }
                        )
                    }) {
                    val borderMod = if (editingMode == FontSizeEditMode.Primary) Modifier
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp) else Modifier
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(borderMod),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        topTimes.forEachIndexed { index, time ->
                            FocusTimeDisplay(
                                time,
                                isPrimary = index == 0,
                                textScale = animatedTextScalePrimary
                            )
                        }
                    }
                    if (editingMode == FontSizeEditMode.Primary) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${(textScalePrimary * 100).roundToInt()}%",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            SmallFloatingActionButton(
                                onClick = { setPrimary(textScalePrimary + 0.1f) },
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) { Icon(Icons.Default.Add, "Augmenter la taille") }
                            SmallFloatingActionButton(
                                onClick = { setPrimary(textScalePrimary - 0.1f) },
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) { Icon(Icons.Default.Remove, "Réduire la taille") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom 3 times
            val bottomTimes = times.drop(1).take(3)
            if (bottomTimes.isNotEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                editingMode = FontSizeEditMode.Secondary
                            },
                            onTap = {
                                if (editingMode != FontSizeEditMode.None) editingMode =
                                    FontSizeEditMode.None
                            }
                        )
                    }) {
                    val borderMod = if (editingMode == FontSizeEditMode.Secondary) Modifier
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp) else Modifier
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(borderMod),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        bottomTimes.forEach { time ->
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                TimeDisplayExpo(
                                    time,
                                    lineColor,
                                    textScale = animatedTextScaleSecondary
                                )
                            }
                        }
                    }
                    if (editingMode == FontSizeEditMode.Secondary) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${(textScaleSecondary * 100).roundToInt()}%",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(1.dp)
                        ) {
                            SmallFloatingActionButton(
                                onClick = { setSecondary(textScaleSecondary + 0.1f) },
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) { Icon(Icons.Default.Add, "Augmenter la taille") }
                            SmallFloatingActionButton(
                                onClick = { setSecondary(textScaleSecondary - 0.1f) },
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) { Icon(Icons.Default.Remove, "Réduire la taille") }
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
