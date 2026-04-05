package xyz.doocode.superbus.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import xyz.doocode.superbus.R

sealed class BadgeContent {
    data class Simple(val text: String) : BadgeContent()
    data class TwoLine(val top: String, val bottom: String) : BadgeContent()
    data class ThreeLine(val top: String, val mid: String, val bottom: String) : BadgeContent()
    object DrawableOnly : BadgeContent()
    data class TextWithOverlay(val text: String) : BadgeContent()
}

private val proxyTgvRegex = Regex("^Proxy TGV (.+)$", RegexOption.IGNORE_CASE)
private val proxyRegex = Regex("^Proxy (.+)$", RegexOption.IGNORE_CASE)

fun resolveBadgeContent(ligneId: String, numLignePublic: String): BadgeContent {
    return when {
        ligneId == "27" -> BadgeContent.DrawableOnly
        ligneId == "98" -> BadgeContent.Simple("C")
        ligneId == "99" -> BadgeContent.Simple("H")
        ligneId == "110" -> BadgeContent.TextWithOverlay("B")
        proxyTgvRegex.matches(numLignePublic) -> {
            val match = proxyTgvRegex.find(numLignePublic)
            val suffix = match?.groupValues?.getOrNull(1) ?: ""
            BadgeContent.ThreeLine("PROXY", "TGV", suffix)
        }
        proxyRegex.matches(numLignePublic) -> {
            val match = proxyRegex.find(numLignePublic)
            val letter = match?.groupValues?.getOrNull(1) ?: ""
            BadgeContent.TwoLine("PROXY", letter)
        }
        else -> BadgeContent.Simple(numLignePublic)
    }
}

/**
 * Renders the badge content inside a Box. Declared as a BoxScope extension so
 * [matchParentSize] is available for the drawable overlay cases.
 *
 * Text-based cases apply their own padding so the parent Box background extends
 * to the full badge area; the overlay image can then fill it with [matchParentSize].
 */
@Composable
fun BoxScope.BadgeBoxContent(
    content: BadgeContent,
    textColor: Color,
    fontSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    when (content) {
        is BadgeContent.Simple -> {
            Text(
                text = content.text,
                color = textColor,
                fontWeight = fontWeight,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .widthIn(min = 28.dp)
            )
        }
        is BadgeContent.TwoLine -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .widthIn(min = 36.dp)
            ) {
                Text(
                    text = content.top,
                    color = textColor,
                    fontWeight = fontWeight,
                    fontSize = 8.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = content.bottom,
                    color = textColor,
                    fontWeight = fontWeight,
                    fontSize = 30.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
        is BadgeContent.ThreeLine -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .widthIn(min = 36.dp)
            ) {
                Text(
                    text = content.top,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = content.mid,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = content.bottom,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
        is BadgeContent.DrawableOnly -> {
            Image(
                painter = painterResource(id = R.drawable.citadelle),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
        is BadgeContent.TextWithOverlay -> {
            Text(
                text = content.text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                    .widthIn(min = 28.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.planb),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun LineBadge(
    numLigne: String,
    couleurFond: String,
    couleurTexte: String,
    ligneId: String = "",
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(6.dp)
) {
    val backgroundColor = remember(couleurFond) {
        parseColorSafe(couleurFond, Color.Gray)
    }
    val textColor = remember(couleurTexte) {
        parseColorSafe(couleurTexte, Color.White)
    }
    val content = remember(ligneId, numLigne) { resolveBadgeContent(ligneId, numLigne) }

    Box(
        modifier = modifier
            .clip(shape)
            .background(color = backgroundColor)
            .then(
                // DrawableOnly has no text child to size the Box, so enforce a minimum
                if (content is BadgeContent.DrawableOnly)
                    Modifier.sizeIn(minWidth = 36.dp, minHeight = 36.dp)
                else
                    Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        BadgeBoxContent(content = content, textColor = textColor)
    }
}

fun parseColorSafe(hexColor: String, default: Color): Color {
    return try {
        val colorString = if (hexColor.startsWith("#")) hexColor else "#$hexColor"
        Color(colorString.toColorInt())
    } catch (e: Exception) {
        default
    }
}

@Preview
@Composable
fun LineBadgePreview() {
    MaterialTheme {
        LineBadge(numLigne = "3", couleurFond = "#D32F2F", couleurTexte = "#FFFFFF")
    }
}
