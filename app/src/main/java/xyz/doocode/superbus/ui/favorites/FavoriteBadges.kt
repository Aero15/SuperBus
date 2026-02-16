package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Ligne
import xyz.doocode.superbus.ui.components.parseColorSafe

@Composable
fun EnlargedLineBadge(line: Ligne) {
    val bgColor = parseColorSafe(line.couleurFond, MaterialTheme.colorScheme.primary)
    val textColor = parseColorSafe(line.couleurTexte, MaterialTheme.colorScheme.onPrimary)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = line.numLignePublic,
            color = textColor,
            style = MaterialTheme.typography.displaySmall, // Large text
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SmallLineBadge(line: Ligne) {
    val bgColor = parseColorSafe(line.couleurFond, MaterialTheme.colorScheme.primary)
    val textColor = parseColorSafe(line.couleurTexte, MaterialTheme.colorScheme.onPrimary)

    Box(
        modifier = Modifier
            .size(40.dp) // Fixed small size
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = line.numLigne,
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
