package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ExitConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (doNotAskExitAgain: Boolean) -> Unit
) {
    var doNotAskExitAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Quitter ?") },
        text = {
            Column {
                Text("Des annonces vocales sont en cours. En quittant, elles seront supprimées.")
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { doNotAskExitAgain = !doNotAskExitAgain },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = doNotAskExitAgain,
                        onCheckedChange = null
                    )
                    Text(
                        text = "Ne plus demander",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(doNotAskExitAgain) }) {
                Text("Quitter")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Annuler")
            }
        }
    )
}
