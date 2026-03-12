package xyz.doocode.superbus.ui.lines

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import xyz.doocode.superbus.core.dto.Ligne
import xyz.doocode.superbus.core.dto.Variante

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinesScreen(
    onNavigateBack: () -> Unit,
    onVariantSelected: (Ligne, Variante) -> Unit,
    viewModel: LinesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedLine by remember { mutableStateOf<Ligne?>(null) }
    val sheetState = rememberModalBottomSheetState()

    if (selectedLine != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedLine = null },
            sheetState = sheetState
        ) {
            LineVariantsSheetContent(
                line = selectedLine!!,
                onVariantClick = { variant ->
                    onVariantSelected(selectedLine!!, variant)
                    selectedLine = null
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState.isSearching) {
                        TextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::onSearchQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Rechercher"
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    } else {
                        Text(
                            "Lignes",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isSearching) viewModel.toggleSearch() else onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (uiState.isSearching) "Fermer recherche" else "Retour"
                        )
                    }
                },
                actions = {
                    if (uiState.isSearching) {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Effacer")
                            }
                        }
                    } else {
                        IconButton(onClick = viewModel::toggleSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Rechercher")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    Button(
                        onClick = viewModel::retry,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                    ) {
                        Text("Réessayer")
                    }
                }

                else -> {
                    LinesGrid(
                        groupedLines = uiState.lineGroups,
                        collapsedSections = uiState.collapsedSections,
                        onToggleSection = viewModel::toggleSection,
                        onLineClick = { line -> selectedLine = line },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
