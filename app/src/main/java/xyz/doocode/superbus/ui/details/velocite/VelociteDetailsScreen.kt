package xyz.doocode.superbus.ui.details.velocite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.details.velocite.components.VelociteAddressCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteCapacityChartCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteRecap
import xyz.doocode.superbus.ui.details.velocite.components.VelociteStatusCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteDetailsScreen(
        stationName: String,
        viewModel: VelociteDetailsViewModel,
        onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val formattedName =
            stationName
                    .replace(Regex("^\\d+\\s*-\\s*"), "")
                    .replace(" (CB)", "")
                    .lowercase()
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text(text = formattedName) },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Retour"
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor =
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                )
                )
            }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is VelociteDetailsUiState.Loading -> {
                    LoadingView()
                }
                is VelociteDetailsUiState.Error -> {
                    ErrorView(message = state.message, onRetry = { viewModel.reload() })
                }
                is VelociteDetailsUiState.Success -> {
                    Column(
                            modifier =
                                    Modifier.fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VelociteRecap(station = state.station)
                        VelociteCapacityChartCard(station = state.station)
                        VelociteStatusCard(station = state.station)
                        VelociteAddressCard(station = state.station)
                    }
                }
            }
        }
    }
}
