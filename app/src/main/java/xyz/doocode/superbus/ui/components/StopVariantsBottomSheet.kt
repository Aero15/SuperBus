package xyz.doocode.superbus.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tram
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Arret

/**
 * Bottom sheet partagé affichant les variantes (quais) d'une station groupée.
 *
 * - Utilisation minimale (écran détails) : seuls [stop], [onDismissRequest],
 *   [onGroupedClick] et [onDuplicateClick] sont requis.
 * - Utilisation complète (écran recherche) : passer en plus [isGroupedFavorite],
 *   [isDuplicateFavorite], [onToggleGroupedFavorite], [onToggleDuplicateFavorite]
 *   et [onFillQuery] pour activer les indicateurs favoris et le menu contextuel
 *   au appui long.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StopVariantsBottomSheet(
    stop: Arret,
    onDismissRequest: () -> Unit,
    onGroupedClick: () -> Unit,
    onDuplicateClick: (Arret) -> Unit,
    isGroupedFavorite: Boolean = false,
    isDuplicateFavorite: (Arret) -> Boolean = { false },
    onToggleGroupedFavorite: (() -> Unit)? = null,
    onToggleDuplicateFavorite: ((Arret) -> Unit)? = null,
    onFillQuery: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState()
    val hasActions = onFillQuery != null

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            Text(
                text = stop.nom,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            HorizontalDivider()

            LazyColumn(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {

                // 1. Item « Recommandé » — ouverture par nom (tous les quais)
                item {
                    var showMenu by remember { mutableStateOf(false) }

                    val groupedIcon =
                        if (isGroupedFavorite) Icons.Default.Favorite else Icons.Default.Search
                    val groupedIconTint =
                        if (isGroupedFavorite) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary

                    Box {
                        ListItem(
                            headlineContent = {
                                val parts = stop.nom.split(" - ", limit = 2)
                                if (parts.size == 2) {
                                    Column {
                                        Text(
                                            text = parts[0],
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                        Text(
                                            text = parts[1],
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                } else {
                                    Text(
                                        text = stop.nom,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            supportingContent = { Text("Voir les horaires de tous les quais") },
                            leadingContent = {
                                Icon(
                                    imageVector = groupedIcon,
                                    contentDescription = null,
                                    tint = groupedIconTint
                                )
                            },
                            trailingContent = {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = "Recommandé",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.3f
                                )
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .then(
                                    if (hasActions) {
                                        Modifier.combinedClickable(
                                            onClick = onGroupedClick,
                                            onLongClick = { showMenu = true }
                                        )
                                    } else {
                                        Modifier.clickable { onGroupedClick() }
                                    }
                                )
                        )

                        if (hasActions && onToggleGroupedFavorite != null && onFillQuery != null) {
                            StopActionsContainer(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                stopName = stop.nom,
                                stopId = stop.id,
                                isFavorite = isGroupedFavorite,
                                onToggleFavorite = onToggleGroupedFavorite,
                                onFillQuery = onFillQuery
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }

                // 2. Items individuels — un par quai (ID)
                items(stop.duplicates) { duplicate ->
                    var showMenu by remember { mutableStateOf(false) }

                    val isFav = isDuplicateFavorite(duplicate)
                    val isTram = duplicate.id.startsWith("t_")

                    val dupIcon = when {
                        isFav -> Icons.Default.Favorite
                        isTram -> Icons.Default.Tram
                        else -> Icons.Default.DirectionsBus
                    }
                    val dupIconTint = when {
                        isFav -> Color(0xFFE91E63)
                        isTram -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.secondary
                    }

                    Box {
                        ListItem(
                            leadingContent = {
                                Icon(
                                    imageVector = dupIcon,
                                    contentDescription = null,
                                    tint = dupIconTint
                                )
                            },
                            headlineContent = {
                                val parts = duplicate.nom.split(" - ", limit = 2)
                                if (parts.size == 2) {
                                    Column {
                                        Text(
                                            text = parts[0],
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                        Text(
                                            text = parts[1],
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                } else {
                                    Text(
                                        text = duplicate.nom,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            },
                            trailingContent = {
                                Text(
                                    text = "#${duplicate.id}",
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.then(
                                if (hasActions) {
                                    Modifier.combinedClickable(
                                        onClick = { onDuplicateClick(duplicate) },
                                        onLongClick = { showMenu = true }
                                    )
                                } else {
                                    Modifier.clickable { onDuplicateClick(duplicate) }
                                }
                            )
                        )

                        if (hasActions && onToggleDuplicateFavorite != null && onFillQuery != null) {
                            StopActionsContainer(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                stopName = duplicate.nom,
                                stopId = duplicate.id,
                                isFavorite = isFav,
                                onToggleFavorite = { onToggleDuplicateFavorite(duplicate) },
                                onFillQuery = onFillQuery
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        }
    }
}
