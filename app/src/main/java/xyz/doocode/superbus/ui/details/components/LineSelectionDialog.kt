package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.Temps
import xyz.doocode.superbus.ui.components.LineBadge

@Composable
fun LineSelectionDialog(
    groupedArrivals: Map<String, List<Temps>>,
    ttsSubscriptionsKeys: Set<String>,
    onDismissRequest: () -> Unit,
    onToggleTtsSubscription: (key: String, numLigne: String, destination: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Annonces sonores") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val keysList = groupedArrivals.keys.toList()
                items(keysList.size) { index ->
                    val key = keysList[index]
                    val parts = key.split("|")
                    val numLigne = parts.getOrNull(0) ?: "?"
                    val destination = parts.getOrNull(1) ?: "?"
                    val firstArrival = groupedArrivals[key]?.firstOrNull()
                    val isSubscribed = key in ttsSubscriptionsKeys
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggleTtsSubscription(key, numLigne, destination)
                            }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LineBadge with iOS-style notification checkmark in top-right corner
                        Box(
                            modifier = Modifier.padding(top = 6.dp, end = 6.dp)
                        ) {
                            LineBadge(
                                numLigne = firstArrival?.numLignePublic ?: numLigne,
                                couleurFond = firstArrival?.couleurFond ?: "",
                                couleurTexte = firstArrival?.couleurTexte ?: "",
                                ligneId = firstArrival?.idLigne ?: "",
                                modifier = Modifier.size(48.dp)
                            )
                            if (isSubscribed) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 6.dp, y = (-6).dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Annonce activée",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = destination,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Terminer")
            }
        }
    )
}