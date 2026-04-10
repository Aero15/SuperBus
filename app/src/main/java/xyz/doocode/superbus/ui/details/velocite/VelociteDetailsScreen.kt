package xyz.doocode.superbus.ui.details.velocite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.util.formatVelociteStationName
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.details.StopDetailsLoadingView
import xyz.doocode.superbus.ui.details.velocite.components.VelociteAddressCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteCapacityChartCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteRecap
import xyz.doocode.superbus.ui.details.velocite.components.VelociteStatusCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteDetailsScreen(
    stationName: String, viewModel: VelociteDetailsViewModel, onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.startPolling()
                Lifecycle.Event.ON_PAUSE -> viewModel.stopPolling()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val formattedName = formatVelociteStationName(stationName)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = formattedName) }, navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour"
                    )
                }
            })
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is VelociteDetailsUiState.Loading -> {
                    StopDetailsLoadingView()
                }

                is VelociteDetailsUiState.Error -> {
                    ErrorView(message = state.message, onRetry = { viewModel.reload() })
                }

                is VelociteDetailsUiState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VelociteRecap(station = state.station, expanded = true)
                        VelociteCapacityChartCard(station = state.station)
                        VelociteStatusCard(station = state.station)
                        VelociteAddressCard(station = state.station)
                    }
                }
            }
        }
    }
}
