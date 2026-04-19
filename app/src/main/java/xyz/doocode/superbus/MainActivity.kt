package xyz.doocode.superbus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import xyz.doocode.superbus.ui.home.*
import xyz.doocode.superbus.ui.menu.MenuScreen
import xyz.doocode.superbus.ui.favorites.FavoritesScreen
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.details.velocite.VelociteDetailsActivity
import xyz.doocode.superbus.core.dto.ginko.FavoriteStation
import xyz.doocode.superbus.ui.search.SearchScreen
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class MainActivity : ComponentActivity() {
    private var launchRequest by mutableStateOf(LaunchRequest())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            SuperBusTheme {
                SuperBusApp(launchRequest = launchRequest)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        launchRequest = LaunchRequest(
            destination = intent?.getStringExtra(EXTRA_DESTINATION),
            searchQuery = intent?.getStringExtra(EXTRA_SEARCH_QUERY),
            requestId = System.currentTimeMillis()
        )
    }

    companion object {
        const val EXTRA_DESTINATION = "extra_destination"
        const val EXTRA_SEARCH_QUERY = "extra_search_query"
    }
}

data class LaunchRequest(
    val destination: String? = null,
    val searchQuery: String? = null,
    val requestId: Long = 0L
)

@PreviewScreenSizes
@Composable
fun SuperBusApp(launchRequest: LaunchRequest = LaunchRequest()) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.FAVORITES) }
    var autoFocusSearch by rememberSaveable { mutableStateOf(false) }
    var autoVelociteFilter by rememberSaveable { mutableStateOf(false) }
    var pendingSearchQuery by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(launchRequest.requestId) {
        if (
            launchRequest.destination == AppDestinations.SEARCH.name ||
            !launchRequest.searchQuery.isNullOrBlank()
        ) {
            currentDestination = AppDestinations.SEARCH
            autoFocusSearch = true
            autoVelociteFilter = false
            pendingSearchQuery = launchRequest.searchQuery
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                val isSelected = it == currentDestination
                item(
                    icon = {
                        Icon(
                            imageVector = if (isSelected) it.selectedIcon else it.unselectedIcon,
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label, textAlign = TextAlign.Center) },
                    selected = isSelected,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            val modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            when (currentDestination) {
                //AppDestinations.MAP -> MapScreen(modifier)
                AppDestinations.FAVORITES -> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    FavoritesScreen(
                        modifier = modifier,
                        onSearchClick = {
                            autoFocusSearch = true
                            currentDestination = AppDestinations.SEARCH
                        },
                        onStationClick = { station ->
                            if (station.effectiveKind == FavoriteStation.KIND_VELOCITE) {
                                val intent = android.content.Intent(
                                    context,
                                    VelociteDetailsActivity::class.java
                                )
                                intent.putExtra(
                                    VelociteDetailsActivity.EXTRA_STATION_ID,
                                    station.id.toInt()
                                )
                                intent.putExtra(
                                    VelociteDetailsActivity.EXTRA_STATION_NAME,
                                    station.name
                                )
                                context.startActivity(intent)
                            } else {
                                val intent =
                                    android.content.Intent(context, StopDetailsActivity::class.java)
                                intent.putExtra(StopDetailsActivity.EXTRA_STOP_ID, station.id)
                                intent.putExtra(StopDetailsActivity.EXTRA_STOP_NAME, station.name)
                                intent.putExtra(
                                    StopDetailsActivity.EXTRA_DETAILS_FROM_ID,
                                    station.detailsFromId
                                )
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                AppDestinations.SEARCH -> SearchScreen(
                    modifier = modifier,
                    focusOnStart = autoFocusSearch,
                    onFocusConsumed = { autoFocusSearch = false },
                    initialQuery = pendingSearchQuery,
                    onInitialQueryConsumed = { pendingSearchQuery = null },
                    initialFilter = if (autoVelociteFilter) xyz.doocode.superbus.ui.search.components.SearchFilterOption.VELOCITE else xyz.doocode.superbus.ui.search.components.SearchFilterOption.NONE,
                    onFilterConsumed = { autoVelociteFilter = false }
                )

                AppDestinations.TRAFFIC -> TrafficScreen(modifier)
                AppDestinations.MENU -> MenuScreen(modifier, onNavigateTo = { dest ->
                    if (dest == AppDestinations.SEARCH) {
                        autoFocusSearch = true
                    }
                    currentDestination = dest
                }, onNavigateToVelociteSearch = {
                    autoVelociteFilter = true
                    currentDestination = AppDestinations.SEARCH
                })
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    //MAP("Carte", Icons.Default.Place, Icons.Outlined.Place),
    FAVORITES("Favoris", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder),
    SEARCH("Chercher", Icons.Default.Search, Icons.Outlined.Search),
    TRAFFIC("Infos", Icons.Default.Info, Icons.Outlined.Info),
    MENU("Menu", Icons.Default.Menu, Icons.Outlined.Menu),
}
