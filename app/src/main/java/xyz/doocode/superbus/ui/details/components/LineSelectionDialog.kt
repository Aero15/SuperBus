package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LineSelectionDialog(
    keysList: List<String>,
    ttsSubscriptionsKeys: Set<String>,
    onDismissRequest: () -> Unit,
    onToggleTtsSubscription: (key: String, numLigne: String, destination: String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Activer l'annonce vocale") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(keysList.size) { index ->
                    val key = keysList[index]
                    val parts = key.split("|")
                    val isSubscribed = key in ttsSubscriptionsKeys
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggleTtsSubscription(
                                    key,
                                    parts.getOrNull(0) ?: "?",
                                    parts.getOrNull(1) ?: "?"
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSubscribed,
                            onCheckedChange = null // Handled by Row click
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${parts.getOrNull(0)} vers ${parts.getOrNull(1)}",
                            style = MaterialTheme.typography.bodyMedium
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