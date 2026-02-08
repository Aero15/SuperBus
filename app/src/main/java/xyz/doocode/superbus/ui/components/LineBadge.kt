package xyz.doocode.superbus.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

@Composable
fun LineBadge(
    numLigne: String,
    couleurFond: String,
    couleurTexte: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(couleurFond) {
        parseColorSafe(couleurFond, Color.Gray)
    }

    val textColor = remember(couleurTexte) {
        parseColorSafe(couleurTexte, Color.White)
    }

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .widthIn(min = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = numLigne,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

fun parseColorSafe(hexColor: String, default: Color): Color {
    return try {
        // Handle cases where the API might return colors without # or weird formats
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
