package xyz.doocode.superbus.ui.menu

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.doocode.superbus.AppDestinations

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    onNavigateTo: (AppDestinations) -> Unit = {}
) {
    val viewModel: MenuViewModel = viewModel()
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshGreeting()
    }

    val onFeatureClick: (MenuFeature) -> Unit = { feature ->
        when (feature) {
            MenuFeature.FAVORITES -> onNavigateTo(AppDestinations.FAVORITES)
            MenuFeature.MAP -> onNavigateTo(AppDestinations.MAP)
            MenuFeature.TRAFFIC -> onNavigateTo(AppDestinations.TRAFFIC)
            else -> Toast.makeText(context, "TODO: ${feature.label}", Toast.LENGTH_SHORT).show()
        }
    }

    val onServiceClick: (ExternalService) -> Unit = { service ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(service.url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur lien", Toast.LENGTH_SHORT).show()
        }
    }
    
    val onUtilityClick: (UtilityItem) -> Unit = { utility ->
        Toast.makeText(context, "TODO: ${utility.label}", Toast.LENGTH_SHORT).show()
    }

    MenuScreenContent(
        modifier = modifier,
        uiState = uiState,
        onNavigateTo = onNavigateTo,
        onClearHistory = viewModel::clearHistory,
        onFeatureClick = onFeatureClick,
        onServiceClick = onServiceClick,
        onUtilityClick = onUtilityClick
    )
}

@Composable
fun MenuScreenContent(
    modifier: Modifier = Modifier,
    uiState: MenuUiState,
    onNavigateTo: (AppDestinations) -> Unit,
    onClearHistory: () -> Unit,
    onFeatureClick: (MenuFeature) -> Unit,
    onServiceClick: (ExternalService) -> Unit,
    onUtilityClick: (UtilityItem) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight
        val isWideScreen = maxWidth > 600.dp && isLandscape

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    MenuHeader(greeting = uiState.userGreeting, isCompact = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    FakeSearchBar(onClick = { onNavigateTo(AppDestinations.SEARCH) })
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Explorer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    MainFeaturesGrid(onItemClick = onFeatureClick, columns = 2)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Services de mobilité",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ServicesGrid(onItemClick = onServiceClick)

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Tableau de bord",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    DashboardSection(
                        historyCount = uiState.historyCount,
                        cacheSize = uiState.cacheSizeMb,
                        onClearHistory = onClearHistory,
                        onTrafficClick = { onNavigateTo(AppDestinations.TRAFFIC) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        UtilityList(onItemClick = onUtilityClick)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    MenuHeader(greeting = uiState.userGreeting, isCompact = true)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        FakeSearchBar(onClick = { onNavigateTo(AppDestinations.SEARCH) })
                    }
                }

                item {
                    Text(
                        text = "Explorer",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    MainFeaturesGrid(onItemClick = onFeatureClick, columns = 2)
                }

                item {
                    Text(
                        text = "Services",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    ServicesRow(onItemClick = onServiceClick)
                }

                item {
                    Text(
                        text = "Tableau de bord",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    DashboardSection(
                        historyCount = uiState.historyCount,
                        cacheSize = uiState.cacheSizeMb,
                        onClearHistory = onClearHistory,
                        onTrafficClick = { onNavigateTo(AppDestinations.TRAFFIC) }
                    )
                }

                item {
                    Text(
                        text = "Application",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    UtilityList(onItemClick = onUtilityClick)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MenuScreenPreview() {
    MaterialTheme {
        MenuScreenContent(
            uiState = MenuUiState(historyCount = 12, cacheSizeMb = 4.5),
            onNavigateTo = {},
            onClearHistory = {},
            onFeatureClick = {},
            onServiceClick = {},
            onUtilityClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MenuScreenDarkPreview() {
    MaterialTheme {
        MenuScreenContent(
            uiState = MenuUiState(historyCount = 12, cacheSizeMb = 4.5),
            onNavigateTo = {},
            onClearHistory = {},
            onFeatureClick = {},
            onServiceClick = {},
            onUtilityClick = {}
        )
    }
}
