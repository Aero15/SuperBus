package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.tts.TtsSettings
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TtsSettingsDialog(
    currentSettings: TtsSettings,
    onDismiss: () -> Unit,
    onSave: (TtsSettings) -> Unit,
    onTest: (TtsSettings) -> Unit = {}
) {
    var speechRate by remember { mutableFloatStateOf(currentSettings.speechRate) }
    var pitch by remember { mutableFloatStateOf(currentSettings.pitch) }
    var volume by remember { mutableFloatStateOf(currentSettings.volume) }
    var announceSecondArrival by remember { mutableStateOf(currentSettings.announceSecondArrival) }
    var announceSecondArrivalOnlyUnder10Min by remember { mutableStateOf(currentSettings.announceSecondArrivalOnlyUnder10Min) }
    var askBeforeExit by remember { mutableStateOf(currentSettings.askBeforeExit) }

    val languages = remember {
        listOf(
            Locale.FRANCE to "Français (France)",
            Locale.Builder().setLanguage("fr").setRegion("BE").build() to "Français (Belgique)",
            Locale.Builder().setLanguage("fr").setRegion("CH").build() to "Français (Suisse)",
            Locale.Builder().setLanguage("fr").setRegion("CA").build() to "Français (Canada)",
            Locale.ENGLISH to "English",
        )
    }
    var selectedLocale by remember {
        mutableStateOf(
            languages.firstOrNull { it.first.language == currentSettings.language.language && it.first.country == currentSettings.language.country }?.first
                ?: languages.first().first
        )
    }
    var languageMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Réglages de la synthèse vocale") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Volume
                Text(
                    text = "Volume : ${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = volume,
                    onValueChange = { volume = it },
                    valueRange = 0.1f..1.0f,
                    steps = 9
                )

                // Speech rate
                Text(
                    text = "Vitesse : ${String.format(Locale.FRANCE, "%.1f", speechRate)}x",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    valueRange = 0.5f..2.0f,
                    steps = 5
                )

                // Pitch
                Text(
                    text = "Tonalité : ${String.format(Locale.FRANCE, "%.1f", pitch)}x",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = pitch,
                    onValueChange = { pitch = it },
                    valueRange = 0.5f..2.0f,
                    steps = 5
                )

                // Language
                Text(
                    text = "Langue",
                    style = MaterialTheme.typography.bodyMedium
                )
                ExposedDropdownMenuBox(
                    expanded = languageMenuExpanded,
                    onExpandedChange = { languageMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = languages.firstOrNull { it.first == selectedLocale }?.second ?: "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false }
                    ) {
                        languages.forEach { (locale, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedLocale = locale
                                    languageMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Announce second arrival
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { announceSecondArrival = !announceSecondArrival },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = announceSecondArrival,
                        onCheckedChange = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Annoncer le 2ème horaire",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (announceSecondArrival) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .clickable {
                                announceSecondArrivalOnlyUnder10Min =
                                    !announceSecondArrivalOnlyUnder10Min
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = announceSecondArrivalOnlyUnder10Min,
                            onCheckedChange = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uniquement lorsque le 1er horaire est <= 10 min",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Ask before exit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { askBeforeExit = !askBeforeExit },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = askBeforeExit,
                        onCheckedChange = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmer avant de quitter",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Test button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        onTest(
                            TtsSettings(
                                speechRate,
                                pitch,
                                selectedLocale,
                                volume,
                                announceSecondArrival,
                                announceSecondArrivalOnlyUnder10Min,
                                askBeforeExit
                            )
                        )
                    }) {
                        Text("Tester la voix")
                    }
                }

                // Reset button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(onClick = {
                        speechRate = 1.0f
                        pitch = 1.0f
                        volume = 1.0f
                        announceSecondArrival = true
                        announceSecondArrivalOnlyUnder10Min = false
                        askBeforeExit = true
                        selectedLocale = Locale.FRANCE
                    }) {
                        Text("Réinitialiser")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    TtsSettings(
                        speechRate,
                        pitch,
                        selectedLocale,
                        volume,
                        announceSecondArrival,
                        announceSecondArrivalOnlyUnder10Min,
                        askBeforeExit
                    )
                )
                onDismiss()
            }) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
