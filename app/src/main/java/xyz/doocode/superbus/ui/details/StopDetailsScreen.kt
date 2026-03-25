package xyz.doocode.superbus.ui.details

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.util.setKeepScreenOn
import androidx.core.content.edit
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    BackHandler(enabled = focusedItemKey != null) {
        focusedItemKey = null
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
                    onFocusedItemChanged = { focusedItemKey = it }
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
