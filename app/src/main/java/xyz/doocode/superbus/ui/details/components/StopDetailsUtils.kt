package xyz.doocode.superbus.ui.details.components

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.material3.MaterialTheme
import androidx.core.graphics.toColorInt

object StopDetailsUtils {

    fun parseLineColor(couleurFond: String, defaultColor: Color = Color.Gray): Color {
        // We need a default color if parsing fails or Compose MaterialTheme isn't available in Utils without Composable context?
        // Actually passing defaultColor is better. Callers will pass MaterialTheme.colorScheme.primary.
        return try {
            Color("#$couleurFond".toColorInt())
        } catch (e: Exception) {
            defaultColor
        }
    }

    fun resolveHighlightLineColor(
        couleurFond: String,
        couleurTexte: String,
        ligneId: String,
        defaultColor: Color = Color.Gray
    ): Color {
        val id = ligneId.toIntOrNull()
        val isPeriurbain = id != null && id in 50..99

        return if (isPeriurbain) {
            parseLineColor(couleurTexte, defaultColor)
        } else {
            parseLineColor(couleurFond, defaultColor)
        }
    }

    @Composable
    fun getGradientColors(lineColor: Color): List<Color> {
        val startColor =
            lineColor.copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)
        val endColor = lineColor.copy(alpha = 0.1f).compositeOver(MaterialTheme.colorScheme.surface)
        return listOf(startColor, endColor)
    }

    fun parseDurationMinutes(timeStr: String, tempsEnSeconde: Int): Int? {
        return if (timeStr.contains("min")) {
            timeStr.filter { it.isDigit() }.toIntOrNull()
        } else if (tempsEnSeconde > 0) {
            tempsEnSeconde / 60
        } else if (timeStr.contains("h") && !timeStr.contains(":")) {
            try {
                val parts = timeStr.lowercase().split("h")
                val h = parts[0].trim().filter { it.isDigit() }.toInt()
                val m = parts.getOrNull(1)?.trim()?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                h * 60 + m
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    @Composable
    fun rememberBlinkingColor(
        isUrgent: Boolean,
        isNear: Boolean,
        defaultColor: Color
    ): State<Color> {
        val blinkTargetColor = if (isUrgent) {
            Color.Red
        } else {
            defaultColor.copy(alpha = 0.3f).compositeOver(MaterialTheme.colorScheme.surface)
        }

        val infiniteTransition = rememberInfiniteTransition(label = "blink")
        return if (isUrgent || isNear) {
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
    }
}

fun Modifier.scrollingGradient(
    colors: List<Color>,
    trigger: Any?,
    shape: Shape
): Modifier = composed {
    val progress = remember { Animatable(1f) }

    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    this
        .clip(shape)
        .drawWithCache {
            val h = size.height
            val totalDistance = 2 * h
            val shift = progress.value * totalDistance

            val doubleColors = colors + colors

            val brush = Brush.verticalGradient(
                colors = doubleColors,
                startY = -shift,
                endY = -shift + (3 * h)
            )

            onDrawBehind {
                drawRect(brush)
            }
        }
}
