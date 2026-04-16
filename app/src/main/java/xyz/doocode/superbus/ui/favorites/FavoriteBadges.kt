package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.ginko.Ligne
import xyz.doocode.superbus.ui.components.BadgeBoxContent
import xyz.doocode.superbus.ui.components.parseColorSafe
import xyz.doocode.superbus.ui.components.resolveBadgeContent

@Composable
fun VelociteBadge(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFB7007A)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
            contentDescription = "Vélocité",
            tint = Color.White,
            modifier = Modifier.size(44.dp)
        )
    }
}

@Composable
fun EnlargedLineBadge(line: Ligne, fontSize: TextUnit = 44.sp) {
    val bgColor = parseColorSafe(line.couleurFond, MaterialTheme.colorScheme.primary)
    val textColor = parseColorSafe(line.couleurTexte, MaterialTheme.colorScheme.onPrimary)
    val content = resolveBadgeContent(line.id, line.numLignePublic)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        BadgeBoxContent(
            content = content,
            textColor = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun SmallLineBadge(line: Ligne) {
    val bgColor = parseColorSafe(line.couleurFond, MaterialTheme.colorScheme.primary)
    val textColor = parseColorSafe(line.couleurTexte, MaterialTheme.colorScheme.onPrimary)
    val content = resolveBadgeContent(line.id, line.numLignePublic)

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        BadgeBoxContent(
            content = content,
            textColor = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
