package xyz.doocode.superbus.ui.details.velocite

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.AppDestinations
import xyz.doocode.superbus.MainActivity
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.core.util.formatVelociteStationName
import xyz.doocode.superbus.core.util.setKeepScreenOn
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.StopVariantsBottomSheet
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.details.StopDetailsLoadingView
import xyz.doocode.superbus.ui.details.velocite.components.VelociteAddressCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteCapacityChartCard
import xyz.doocode.superbus.ui.details.velocite.components.VelociteNearbyStops
import xyz.doocode.superbus.ui.details.velocite.components.VelociteRecap
import xyz.doocode.superbus.ui.details.velocite.components.VelociteStatusCard
import xyz.doocode.superbus.ui.search.components.BusStopItem
import androidx.core.content.edit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteDetailsScreen(
    stationName: String, viewModel: VelociteDetailsViewModel, onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val isLoadingNearbyStops by viewModel.isLoadingNearbyStops.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()

    val prefs =
        remember { context.getSharedPreferences("superbus_app_settings", Context.MODE_PRIVATE) }
    var keepScreenOn by remember {
        mutableStateOf(prefs.getBoolean("keep_screen_on_velocite", false))
    }
    var showMenu by remember { mutableStateOf(false) }
    var showUnfavoriteConfirmation by remember { mutableStateOf(false) }
    var selectedStop by remember { mutableStateOf<Arret?>(null) }
    var showAllNearbyStopsSheet by remember { mutableStateOf(false) }

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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val formattedName = formatVelociteStationName(stationName)

    fun openStopDetails(stop: Arret, fromId: Boolean) {
        val intent = Intent(context, StopDetailsActivity::class.java).apply {
            putExtra(StopDetailsActivity.EXTRA_STOP_NAME, stop.nom)
            putExtra(StopDetailsActivity.EXTRA_STOP_ID, stop.id)
            putExtra(StopDetailsActivity.EXTRA_DETAILS_FROM_ID, fromId)
        }
        context.startActivity(intent)
    }

    fun openSearchWithQuery(query: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, AppDestinations.SEARCH.name)
            putExtra(MainActivity.EXTRA_SEARCH_QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        context.startActivity(intent)
        activity?.finish()
    }

    fun isNearbyStopFavorite(stop: Arret): Boolean {
        val duplicateIds = stop.duplicates.ifEmpty { listOf(stop) }.map { it.id }.toSet()
        return favorites.any { favorite ->
            favorite.effectiveKind == FavoriteStation.KIND_BUS_TRAM && (
                    (!favorite.detailsFromId && favorite.id == stop.id) ||
                            (favorite.detailsFromId && favorite.id in duplicateIds)
                    )
        }
    }

    fun isNearbyDuplicateFavorite(stop: Arret): Boolean {
        return favorites.any { favorite ->
            favorite.effectiveKind == FavoriteStation.KIND_BUS_TRAM &&
                    favorite.detailsFromId && favorite.id == stop.id
        }
    }

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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
            LargeTopAppBar(
                title = {
                    Text(
                        text = formattedName,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
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
                },
                scrollBehavior = scrollBehavior
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
                            .verticalScroll(scrollState)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VelociteRecap(station = state.station, expanded = true)
                        VelociteCapacityChartCard(station = state.station)
                        VelociteStatusCard(station = state.station)
                        VelociteAddressCard(station = state.station)

                        VelociteNearbyStops(
                            nearbyStops = nearbyStops,
                            isLoading = isLoadingNearbyStops,
                            isFavorite = { stop -> isNearbyStopFavorite(stop) },
                            onFillQuery = { query -> openSearchWithQuery(query) },
                            onToggleFavorite = { stop, fromId ->
                                viewModel.toggleFavorite(stop, fromId)
                            },
                            onStopClick = { stop, fromId ->
                                openStopDetails(stop, fromId)
                            },
                            onVariantsClick = { stop ->
                                selectedStop = stop
                            },
                            onShowMoreClick = {
                                showAllNearbyStopsSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAllNearbyStopsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAllNearbyStopsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Bus et trams à proximité",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider()

                nearbyStops.drop(3).forEach { stop ->
                    val hasVariants = stop.duplicates.size > 1
                    BusStopItem(
                        stop = stop,
                        isFavorite = isNearbyStopFavorite(stop),
                        groupDuplicates = hasVariants,
                        onFillQuery = { query -> openSearchWithQuery(query) },
                        onToggleFavorite = {
                            viewModel.toggleFavorite(stop, !hasVariants)
                        },
                        onClick = {
                            showAllNearbyStopsSheet = false
                            openStopDetails(stop, fromId = !hasVariants)
                        },
                        onVariantsClick = {
                            showAllNearbyStopsSheet = false
                            selectedStop = stop
                        }
                    )
                }
            }
        }
    }

    if (selectedStop != null) {
        StopVariantsBottomSheet(
            stop = selectedStop!!,
            onDismissRequest = { selectedStop = null },
            onGroupedClick = {
                val stop = selectedStop!!
                openStopDetails(stop, fromId = false)
                selectedStop = null
            },
            onDuplicateClick = { duplicate ->
                openStopDetails(duplicate, fromId = true)
                selectedStop = null
            },
            isGroupedFavorite = isNearbyStopFavorite(selectedStop!!),
            isDuplicateFavorite = { duplicate -> isNearbyDuplicateFavorite(duplicate) },
            onToggleGroupedFavorite = {
                viewModel.toggleFavorite(selectedStop!!, false)
            },
            onToggleDuplicateFavorite = { duplicate ->
                viewModel.toggleFavorite(duplicate, true)
            },
            onFillQuery = { query -> openSearchWithQuery(query) }
        )
    }
}
