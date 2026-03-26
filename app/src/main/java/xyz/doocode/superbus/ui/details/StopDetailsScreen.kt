package xyz.doocode.superbus.ui.details

import android.app.Activity
import android.content.Context
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RecordVoiceOver
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
import xyz.doocode.superbus.ui.details.components.StopDetailsUtils
import xyz.doocode.superbus.ui.details.components.TtsSettingsDialog
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
    val ttsSubscriptions by viewModel.ttsManager.activeSubscriptions.collectAsState()
    val currentlySpeakingKey by viewModel.ttsManager.currentlySpeakingKey.collectAsState()
    val title = stopName ?: "Arrêt"

    val isSingleItem = (uiState as? StopDetailsUiState.Success)?.groupedArrivals?.size == 1

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullRefreshState = rememberPullToRefreshState()

    // Keep Screen On Logic
    val context = LocalContext.current
    val activity = context as? Activity
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
    var focusedItemKey by remember { mutableStateOf<String?>(null) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var showTtsSettings by remember { mutableStateOf(false) }
    var doNotAskExitAgain by remember { mutableStateOf(false) }
    var showLineSelectionDialog by remember { mutableStateOf(false) }

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
                keysList = successState.groupedArrivals.keys.toList(),
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

    // TTS Settings dialog
    if (showTtsSettings) {
        TtsSettingsDialog(
            currentSettings = viewModel.getTtsSettings(),
            onDismiss = { showTtsSettings = false },
            onSave = { viewModel.saveTtsSettings(it) },
            onTest = { testSettings -> viewModel.ttsManager.testTTS(testSettings) }
        )
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            if (uiState is StopDetailsUiState.Success) {
                val successState = uiState as StopDetailsUiState.Success
                if (successState.groupedArrivals.isNotEmpty()) {
                    val currentFabKey = if (isSingleItem) {
                        successState.groupedArrivals.keys.firstOrNull()
                    } else {
                        focusedItemKey
                    }
                    val currentFabArrivals = currentFabKey?.let { successState.groupedArrivals[it] }

                    val isFabSubscribed = if (currentFabKey != null) {
                        currentFabKey in ttsSubscriptions
                    } else {
                        ttsSubscriptions.isNotEmpty()
                    }

                    val defaultFabContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    val defaultFabContentColor = MaterialTheme.colorScheme.onSurface

                    val targetLigneColor =
                        if (currentFabKey != null && currentFabArrivals != null && isFabSubscribed) {
                            StopDetailsUtils.parseLineColor(
                                couleurFond = currentFabArrivals.firstOrNull()?.couleurFond ?: "",
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
                                couleurFond = currentFabArrivals.firstOrNull()?.couleurTexte ?: "",
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
                            modifier = Modifier.defaultMinSize(minWidth = 56.dp, minHeight = 56.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFabSubscribed) Icons.Default.RecordVoiceOver else Icons.Default.VoiceOverOff,
                                contentDescription = null
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
                    IconButton(onClick = viewModel::toggleFavorite) {
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
                                    text = { Text(if (forcedExpandState != false) "Tout réduire" else "Tout développer") },
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
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Réglages vocaux") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showTtsSettings = true
                                    showMenu = false
                                }
                            )
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
                    onRetry = { viewModel.init(stopName, stopId) },
                    onItemLongClick = { focusedItemKey = it }
                )
            }
        }
    }
}
