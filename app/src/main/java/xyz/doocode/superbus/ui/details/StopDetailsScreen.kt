package xyz.doocode.superbus.ui.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.util.setKeepScreenOn
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.ui.details.components.StopDetailsUtils
import xyz.doocode.superbus.ui.details.components.TtsSettingsDialog
import xyz.doocode.superbus.ui.details.velocite.VelociteDetailsActivity
import androidx.core.content.edit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StopDetailsScreen(
    stopName: String?,
    stopId: String?,
    detailsFromId: Boolean = true,
    onBackClick: () -> Unit,
    viewModel: StopDetailsViewModel = viewModel()
) {
    LaunchedEffect(stopName, stopId) {
        viewModel.init(stopName, stopId, detailsFromId)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                viewModel.startAutoRefresh()
            } else if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.stopAutoRefresh()
                if (viewModel.hasTtsSubscriptions()) {
                    viewModel.announceTtsPause()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopAutoRefresh()
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val nearbyStops by viewModel.nearbyStops.collectAsState()
    val isLoadingNearbyStops by viewModel.isLoadingNearbyStops.collectAsState()
    val ttsSubscriptions by viewModel.ttsManager.activeSubscriptions.collectAsState()
    val currentlySpeakingKey by viewModel.ttsManager.currentlySpeakingKey.collectAsState()
    val velociteStation by viewModel.velociteStation.collectAsState()
    val title = stopName ?: "Station inconnue"

    val isSingleItem = (uiState as? StopDetailsUiState.Success)?.groupedArrivals?.size == 1

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullRefreshState = rememberPullToRefreshState()

    // Keep Screen On Logic
    val context = LocalContext.current
    val activity = context as? Activity

    val openNearbyStop = { stop: Arret, fromId: Boolean ->
        val intent = Intent(context, StopDetailsActivity::class.java).apply {
            putExtra(StopDetailsActivity.EXTRA_STOP_NAME, stop.nom)
            putExtra(StopDetailsActivity.EXTRA_STOP_ID, stop.id)
            putExtra(StopDetailsActivity.EXTRA_DETAILS_FROM_ID, fromId)
        }
        context.startActivity(intent)
    }
    val prefs =
        remember { context.getSharedPreferences("superbus_app_settings", Context.MODE_PRIVATE) }
    var keepScreenOn by remember {
        mutableStateOf(prefs.getBoolean("keep_screen_on", false))
    }

    LaunchedEffect(keepScreenOn) {
        activity?.setKeepScreenOn(keepScreenOn)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun toggleScreenOn() {
        keepScreenOn = !keepScreenOn
        prefs.edit { putBoolean("keep_screen_on", keepScreenOn) }
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = if (keepScreenOn) "L'écran restera allumé" else "L'option est maintenant désactivée",
                withDismissAction = true,
                duration = SnackbarDuration.Short
            )
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var forcedExpandState by remember { mutableStateOf<Boolean?>(null) }
    var forcedSectionsExpandState by remember { mutableStateOf<Boolean?>(null) }
    var focusedItemKey by remember { mutableStateOf<String?>(null) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showTtsSettings by remember { mutableStateOf(false) }
    var doNotAskExitAgain by remember { mutableStateOf(false) }
    var showLineSelectionDialog by remember { mutableStateOf(false) }
    var showUnfavoriteConfirmation by remember { mutableStateOf(false) }

    // Back handler: confirm exit when TTS subscriptions are active
    val ttsSettings = viewModel.getTtsSettings()
    BackHandler(enabled = true) {
        if (focusedItemKey != null) {
            focusedItemKey = null
        } else if (viewModel.hasTtsSubscriptions() && ttsSettings.askBeforeExit) {
            showExitConfirmation = true
        } else {
            if (viewModel.hasTtsSubscriptions()) viewModel.clearTtsSubscriptions()
            onBackClick()
        }
    }

    // Exit confirmation dialog
    if (showExitConfirmation) {
        xyz.doocode.superbus.ui.details.components.ExitConfirmationDialog(
            onDismissRequest = { showExitConfirmation = false },
            onConfirm = { doNotAskAgain ->
                showExitConfirmation = false
                if (doNotAskAgain) {
                    viewModel.saveTtsSettings(ttsSettings.copy(askBeforeExit = false))
                }
                viewModel.clearTtsSubscriptions()
                onBackClick()
            }
        )
    }

    // Line selection for TTS (List view)
    if (showLineSelectionDialog) {
        val successState = uiState as? StopDetailsUiState.Success
        if (successState != null) {
            xyz.doocode.superbus.ui.details.components.LineSelectionDialog(
                groupedArrivals = successState.groupedArrivals,
                ttsSubscriptionsKeys = ttsSubscriptions.keys,
                onDismissRequest = { showLineSelectionDialog = false },
                onToggleTtsSubscription = { key, numLigne, destination ->
                    viewModel.toggleTtsSubscription(key, numLigne, destination)
                }
            )
        } else {
            showLineSelectionDialog = false
        }
    }

    // Unfavorite confirmation dialog
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

    // TTS Settings dialog
    if (showTtsSettings) {
        TtsSettingsDialog(
            currentSettings = viewModel.getTtsSettings(),
            onDismiss = { showTtsSettings = false },
            onSave = { viewModel.saveTtsSettings(it) },
            onTest = { testSettings -> viewModel.ttsManager.testTTS(testSettings) }
        )
    }

    val showFocusMode =
        uiState is StopDetailsUiState.Success && (isSingleItem || focusedItemKey != null)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            if (uiState is StopDetailsUiState.Success) {
                val successState = uiState as StopDetailsUiState.Success
                if (successState.groupedArrivals.isNotEmpty()) {
                    if (showFocusMode) {
                        // TTS FAB — focus/single-line mode
                        val currentFabKey = if (isSingleItem) {
                            successState.groupedArrivals.keys.firstOrNull()
                        } else {
                            focusedItemKey
                        }
                        val currentFabArrivals =
                            currentFabKey?.let { successState.groupedArrivals[it] }

                        val isFabSubscribed = if (currentFabKey != null) {
                            currentFabKey in ttsSubscriptions
                        } else {
                            ttsSubscriptions.isNotEmpty()
                        }

                        val defaultFabContainerColor =
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        val defaultFabContentColor = MaterialTheme.colorScheme.onSurface

                        val targetLigneColor =
                            if (currentFabKey != null && currentFabArrivals != null && isFabSubscribed) {
                                StopDetailsUtils.parseLineColor(
                                    couleurFond = currentFabArrivals.firstOrNull()?.couleurFond
                                        ?: "",
                                    defaultColor = MaterialTheme.colorScheme.primary
                                )
                            } else if (isFabSubscribed) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                defaultFabContainerColor
                            }

                        val targetTextColor =
                            if (currentFabKey != null && currentFabArrivals != null && isFabSubscribed) {
                                StopDetailsUtils.parseLineColor(
                                    couleurFond = currentFabArrivals.firstOrNull()?.couleurTexte
                                        ?: "",
                                    defaultColor = MaterialTheme.colorScheme.onPrimary
                                )
                            } else if (isFabSubscribed) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                defaultFabContentColor
                            }

                        val fabContainerColor by animateColorAsState(
                            targetValue = targetLigneColor,
                            label = "fabColor"
                        )
                        val fabContentColor by animateColorAsState(
                            targetValue = targetTextColor,
                            label = "fabContent"
                        )

                        Surface(
                            modifier = Modifier
                                .semantics {
                                    role = Role.Button
                                    contentDescription = "Annonce vocale"
                                }
                                .combinedClickable(
                                    onClick = {
                                        if (currentFabKey != null) {
                                            val parts = currentFabKey.split("|")
                                            viewModel.toggleTtsSubscription(
                                                currentFabKey,
                                                parts.getOrNull(0) ?: "?",
                                                parts.getOrNull(1) ?: "?"
                                            )
                                        } else {
                                            showLineSelectionDialog = true
                                        }
                                    },
                                    onLongClick = {
                                        showTtsSettings = true
                                    }
                                ),
                            shape = FloatingActionButtonDefaults.shape,
                            color = fabContainerColor,
                            contentColor = fabContentColor,
                            shadowElevation = 6.dp
                        ) {
                            Box(
                                modifier = Modifier.defaultMinSize(
                                    minWidth = 56.dp,
                                    minHeight = 56.dp
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFabSubscribed) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
                                    contentDescription = null
                                )
                            }
                        }
                    } else {
                        // Fullscreen FAB — list mode
                        FloatingActionButton(
                            onClick = {
                                focusedItemKey = successState.groupedArrivals.keys.firstOrNull()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Plein écran"
                            )
                        }
                    }
                }
            }
        },
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
                    if (title.contains(" - ")) {
                        val parts = title.split(" - ", limit = 2)
                        Column {
                            Text(
                                text = parts[0],
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = parts[1],
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (focusedItemKey != null) {
                            focusedItemKey = null
                        } else if (viewModel.hasTtsSubscriptions()) {
                            showExitConfirmation = true
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
                    if (keepScreenOn) {
                        IconButton(onClick = { toggleScreenOn() }) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Désactiver l'écran toujours allumé",
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
                            Icon(Icons.Default.MoreVert, "Plus d'options")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // ── Écran ──────────────────────────────────────
                            Box(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 10.dp,
                                    bottom = 2.dp
                                )
                            ) {
                                Text(
                                    text = "Écran",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Garder l'écran allumé",
                                        modifier = Modifier.weight(1f)
                                    )
                                },
                                leadingIcon = {
                                    if (keepScreenOn) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = null
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Outlined.Lightbulb,
                                            contentDescription = null
                                        )
                                    }
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
                            if (!isSingleItem) {
                                DropdownMenuItem(
                                    text = { Text(if (focusedItemKey != null) "Quitter le plein écran" else "Plein écran") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (focusedItemKey != null) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        if (focusedItemKey != null) {
                                            focusedItemKey = null
                                        } else {
                                            focusedItemKey =
                                                (uiState as? StopDetailsUiState.Success)
                                                    ?.groupedArrivals?.keys?.firstOrNull()
                                        }
                                        showMenu = false
                                    }
                                )
                            }
                            // ── Annonces sonores ───────────────────────────
                            HorizontalDivider()
                            Box(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 10.dp,
                                    bottom = 2.dp
                                )
                            ) {
                                Text(
                                    text = "Annonces sonores",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Gérer les annonces") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null
                                    )
                                },
                                trailingIcon = {
                                    if (ttsSubscriptions.isNotEmpty()) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Actif",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                onClick = {
                                    showLineSelectionDialog = true
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Réglages des annonces") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showTtsSettings = true
                                    showMenu = false
                                }
                            )
                            // ── Affichage (mode liste uniquement) ──────────
                            if (!showFocusMode) {
                                HorizontalDivider()
                                Box(
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        top = 10.dp,
                                        bottom = 2.dp
                                    )
                                ) {
                                    Text(
                                        text = "Affichage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(if (forcedExpandState != false) "Réduire les cartes" else "Agrandir les cartes") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (forcedExpandState != false) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        forcedExpandState = forcedExpandState == false
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (forcedSectionsExpandState != false) "Réduire les sections" else "Agrandir les sections") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (forcedSectionsExpandState != false) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        forcedSectionsExpandState =
                                            forcedSectionsExpandState == false
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::refresh,
            state = pullRefreshState,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val state = uiState

            // Auto-clear focus if item is gone
            LaunchedEffect(state, focusedItemKey) {
                if (state is StopDetailsUiState.Success && focusedItemKey != null) {
                    if (!state.groupedArrivals.containsKey(focusedItemKey)) {
                        focusedItemKey = null
                    }
                }
            }

            val arrivalsList = remember(state) {
                if (state is StopDetailsUiState.Success) state.groupedArrivals.toList() else emptyList()
            }

            val showFocusMode =
                state is StopDetailsUiState.Success && (state.groupedArrivals.size == 1 || focusedItemKey != null)

            LaunchedEffect(showFocusMode, scrollBehavior) {
                if (showFocusMode) {
                    androidx.compose.animation.core.animate(
                        initialValue = scrollBehavior.state.heightOffset,
                        targetValue = scrollBehavior.state.heightOffsetLimit,
                        animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                    ) { value, _ ->
                        scrollBehavior.state.heightOffset = value
                    }
                }
            }

            if (showFocusMode && arrivalsList.isNotEmpty()) {
                StopDetailsFocusContent(
                    arrivalsList = arrivalsList,
                    focusedItemKey = focusedItemKey,
                    onFocusedItemChanged = { focusedItemKey = it },
                    activeSubscriptionKeys = ttsSubscriptions.keys,
                    currentlySpeakingKey = currentlySpeakingKey,
                    onToggleTts = { key, numLigne, destination ->
                        viewModel.toggleTtsSubscription(key, numLigne, destination)
                    }
                )
            } else {
                StopDetailsListContent(
                    state = state,
                    forcedExpandState = forcedExpandState,
                    forcedSectionsExpandState = forcedSectionsExpandState,
                    velociteStation = velociteStation,
                    onVelociteClick = velociteStation?.let { station ->
                        {
                            val intent =
                                Intent(context, VelociteDetailsActivity::class.java).apply {
                                    putExtra(
                                        VelociteDetailsActivity.EXTRA_STATION_ID,
                                        station.number
                                    )
                                    putExtra(
                                        VelociteDetailsActivity.EXTRA_STATION_NAME,
                                        station.name
                                    )
                                }
                            context.startActivity(intent)
                        }
                    },
                    nearbyStops = nearbyStops,
                    favorites = favorites,
                    isLoadingNearbyStops = isLoadingNearbyStops,
                    onRetry = { viewModel.init(stopName, stopId) },
                    onItemLongClick = { focusedItemKey = it },
                    onNearbyStopClick = openNearbyStop
                )
            }
        }
    }
}
