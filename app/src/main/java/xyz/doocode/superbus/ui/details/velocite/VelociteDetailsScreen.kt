package xyz.doocode.superbus.ui.details.velocite

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.util.formatVelociteStationName
import xyz.doocode.superbus.core.util.setKeepScreenOn
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.details.StopDetailsLoadingView
import xyz.doocode.superbus.ui.details.velocite.components.VelociteAddressCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteCapacityChartCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteRecap
import xyz.doocode.superbus.ui.details.velocite.components.VelociteStatusCard
import androidx.core.content.edit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteDetailsScreen(
    stationName: String, viewModel: VelociteDetailsViewModel, onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity

    val prefs =
        remember { context.getSharedPreferences("superbus_app_settings", Context.MODE_PRIVATE) }
    var keepScreenOn by remember {
        mutableStateOf(prefs.getBoolean("keep_screen_on_velocite", false))
    }
    var showMenu by remember { mutableStateOf(false) }
    var showUnfavoriteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(keepScreenOn) {
        activity?.setKeepScreenOn(keepScreenOn)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun toggleScreenOn() {
        keepScreenOn = !keepScreenOn
        prefs.edit { putBoolean("keep_screen_on_velocite", keepScreenOn) }
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = if (keepScreenOn) "L'écran restera allumé" else "L'option est maintenant désactivée",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

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

    if (showUnfavoriteConfirmation) {
        AlertDialog(
            onDismissRequest = { showUnfavoriteConfirmation = false },
            title = { Text("Retirer des favoris") },
            text = { Text("Voulez-vous retirer cette station de vos favoris ?") },
            confirmButton = {
                TextButton(onClick = {
                    showUnfavoriteConfirmation = false
                    viewModel.toggleFavorite()
                }) {
                    Text("Retirer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnfavoriteConfirmation = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message == "L'écran restera allumé"
                SwipeToDismissBox(
                    state = rememberSwipeToDismissBoxState(),
                    backgroundContent = {}
                ) {
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = if (isSuccess) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        actionColor = if (isSuccess) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        dismissActionContentColor = if (isSuccess) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
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
                actions = {
                    if (keepScreenOn) {
                        IconButton(
                            onClick = { toggleScreenOn() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Écran toujours allumé activé",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(onClick = {
                        if (isFavorite) showUnfavoriteConfirmation = true
                        else viewModel.toggleFavorite()
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Plus d'options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Garder l'écran allumé",
                                        modifier = Modifier.weight(1f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (keepScreenOn) Icons.Default.Lightbulb else Icons.Outlined.Lightbulb,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    if (keepScreenOn) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Activé",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    toggleScreenOn()
                                    showMenu = false
                                }
                            )
                        }
                    }
                }
            )
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
