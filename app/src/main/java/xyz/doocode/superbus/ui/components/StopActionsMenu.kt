package xyz.doocode.superbus.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun StopActionsMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    stopName: String,
    stopId: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onCopyName: () -> Unit,
    onCopyId: () -> Unit,
    onSearchName: () -> Unit,
    onSearchId: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            text = { Text(if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris") },
            leadingIcon = {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFE91E63) else LocalContentColor.current
                )
            },
            onClick = {
                onToggleFavorite()
                onDismissRequest()
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Copier le nom") },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            onClick = {
                onCopyName()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Copier l'ID") },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            onClick = {
                onCopyId()
                onDismissRequest()
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Rechercher ce nom") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            onClick = {
                onSearchName()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Rechercher cet ID") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            onClick = {
                onSearchId()
                onDismissRequest()
            }
        )
    }
}

@Composable
fun StopActionsContainer(
    stopName: String,
    stopId: String,
    isFavorite: Boolean,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onToggleFavorite: () -> Unit,
    onFillQuery: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    StopActionsMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        stopName = stopName,
        stopId = stopId,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onCopyName = { clipboardManager.setText(AnnotatedString(stopName)) },
        onCopyId = { clipboardManager.setText(AnnotatedString(stopId)) },
        onSearchName = { onFillQuery(stopName) },
        onSearchId = { onFillQuery(stopId) }
    )
}
