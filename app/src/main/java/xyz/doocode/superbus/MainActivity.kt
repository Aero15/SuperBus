package xyz.doocode.superbus

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import xyz.doocode.superbus.ui.home.*
import xyz.doocode.superbus.ui.search.SearchScreen
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperBusTheme {
                SuperBusApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun SuperBusApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.MAP) }

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
                AppDestinations.MAP -> MapScreen(modifier)
                AppDestinations.FAVORITES -> FavoritesScreen(modifier)
                AppDestinations.SEARCH -> SearchScreen(modifier)
                AppDestinations.TRAFFIC -> TrafficScreen(modifier)
                AppDestinations.MENU -> MenuScreen(modifier)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    MAP("Carte", Icons.Default.Place, Icons.Outlined.Place),
    FAVORITES("Favoris", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder),
    SEARCH("Chercher", Icons.Default.Search, Icons.Outlined.Search),
    TRAFFIC("Infos", Icons.Default.Info, Icons.Outlined.Info),
    MENU("Menu", Icons.Default.Menu, Icons.Outlined.Menu),
}
