package xyz.doocode.superbus.ui.details

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.core.util.setKeepScreenOn
import xyz.doocode.superbus.ui.components.EmptyDataView
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import androidx.core.content.edit

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

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    val title = stopName ?: "Arrêt"

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullRefreshState = rememberPullToRefreshState()

    // Keep Screen On Logic
    val context = LocalContext.current
    val activity = context as? Activity
    val prefs = remember { context.getSharedPreferences("superbus_app_settings", Context.MODE_PRIVATE) }
    var keepScreenOn by remember { 
        mutableStateOf(prefs.getBoolean("keep_screen_on", false)) 
    }

    LaunchedEffect(keepScreenOn) {
        activity?.setKeepScreenOn(keepScreenOn)
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                actions = {
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
                                    keepScreenOn = !keepScreenOn
                                    prefs.edit { putBoolean("keep_screen_on", keepScreenOn) }
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(/*if (LocalWindowInfo.current.containerSize.width.dp > 600.dp) 2 else*/ 1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                when (val state = uiState) {
                    is StopDetailsUiState.Loading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingView("Chargement des temps d'attente...")
                            }
                        }
                    }

                    is StopDetailsUiState.Error -> {
                        item { ErrorView(state.message) { viewModel.init(stopName, stopId) } }
                    }

                    is StopDetailsUiState.Empty -> {
                        item { EmptyDataView() }
                    }

                    is StopDetailsUiState.Success -> {
                        items(state.groupedArrivals.toList()) { (key, arrivals) ->
                            val parts = key.split("|")
                            ArrivalCard(
                                numLigne = parts.getOrNull(0) ?: "?",
                                destination = parts.getOrNull(1) ?: "?",
                                couleurFond = arrivals.first().couleurFond,
                                couleurTexte = arrivals.first().couleurTexte,
                                times = arrivals
                            )
                        }
                    }
                }
            }
        }
    }
}
