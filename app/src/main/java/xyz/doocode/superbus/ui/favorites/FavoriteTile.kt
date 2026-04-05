package xyz.doocode.superbus.ui.favorites

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.R
import xyz.doocode.superbus.core.dto.FavoriteStation
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun FavoriteTile(
    station: FavoriteStation,
    isEditing: Boolean = false,
    onClick: () -> Unit,
    onRename: () -> Unit = {},
    onRemove: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val rotation by if (isEditing) {
        infiniteTransition.animateFloat(
            initialValue = -2f,
            targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(200, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "rotation"
        )
    } else {
        remember {
            mutableFloatStateOf(0f)
        }
    }

    var showContextMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationZ = if (isEditing) rotation else 0f
            }
            .pointerInput(isEditing) {
                if (!isEditing) {
                    detectTapGestures(
                        onTap = { onClick() },
                        onLongPress = {
                            showContextMenu = true
                        }
                    )
                }
            }
    ) {
        // Tile Box
        Box(
            modifier = Modifier
                .aspectRatio(1f) // Square
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val validLines = station.lines
            val isTramDuo = validLines.size == 2 && validLines.all {
                it.numLignePublic.matches(Regex("T\\d+"))
            }

            if (validLines.isEmpty()) {
                Text(
                    "?",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (validLines.size == 1) {
                // Single Large Badge
                val line = validLines.first()
                val isSingleTram = line.numLignePublic.matches(Regex("T\\d+"))
                if (isSingleTram) {
                    Image(
                        painter = painterResource(R.drawable.tram),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize(if (isSingleTram) 0.72f else 1f)
                        .then(
                            if (isSingleTram) Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = -4.dp)
                                .graphicsLayer { rotationZ = -5f }
                                .border(
                                    3.dp,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .clip(RoundedCornerShape(12.dp))
                            else Modifier.padding(9.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    EnlargedLineBadge(line, fontSize = if (isSingleTram) 38.sp else 44.sp)
                }
            } else if (isTramDuo) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SmallLineBadge(validLines[0])
                            SmallLineBadge(validLines[1])
                        }
                    }
                    Image(
                        painter = painterResource(R.drawable.tram),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Grid 2x2
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    val row1 = validLines.take(2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row1.forEach { SmallLineBadge(it) }
                        if (row1.size < 2) Spacer(modifier = Modifier.size(32.dp))
                    }

                    val remainingCount = validLines.size - 2
                    if (remainingCount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Slot 3
                            val line3 = validLines[2]
                            SmallLineBadge(line3)

                            if (validLines.size == 4) {
                                SmallLineBadge(validLines[3])
                            } else if (validLines.size > 4) {
                                // Counter Badge
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            RoundedCornerShape(6.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${validLines.size - 3}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Empty slot (size was 3)
                                Spacer(modifier = Modifier.size(40.dp))
                            }
                        }
                    } else {
                        // Empty row 2
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Station Name
        Text(
            text = station.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )

        // Context Menu
        if (!isEditing) {
            DropdownMenu(
                expanded = showContextMenu,
                onDismissRequest = { showContextMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Renommer") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        showContextMenu = false
                        onRename()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Retirer des favoris") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = {
                        showContextMenu = false
                        onRemove()
                    }
                )
            }
        }
    }
}
