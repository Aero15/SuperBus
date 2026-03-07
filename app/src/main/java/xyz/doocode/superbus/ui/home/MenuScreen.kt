package xyz.doocode.superbus.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    fun onFeatureClick(feature: MenuFeature) {
        when (feature) {
            MenuFeature.FAVORITES -> onNavigateTo(AppDestinations.FAVORITES)
            MenuFeature.MAP -> onNavigateTo(AppDestinations.MAP)
            MenuFeature.TRAFFIC -> onNavigateTo(AppDestinations.TRAFFIC)
            else -> Toast.makeText(context, "TODO: ${feature.label}", Toast.LENGTH_SHORT).show()
        }
    }

    // Responsive Logic
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Use 2 columns only if screen is wide (>600dp) AND in landscape mode (width > height)
        val isLandscape = maxWidth > maxHeight
        val isWideScreen = maxWidth > 600.dp && isLandscape

        if (isWideScreen) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Left Column: Header, Search, Features
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    //Spacer(modifier = Modifier.height(32.dp))
                    MenuHeader(greeting = uiState.userGreeting, isCompact = true)
                    Spacer(modifier = Modifier.height(12.dp))
                    FakeSearchBar(onClick = { onNavigateTo(AppDestinations.SEARCH) })
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Explorer",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    MainFeaturesGrid(onItemClick = ::onFeatureClick, columns = 2)
                }

                // Right Column: Services, Dashboard, Utility
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .padding(bottom = 80.dp)
                ) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Services de mobilité",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    ServicesGrid(onItemClick = { service ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(service.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur lien", Toast.LENGTH_SHORT).show()
                        }
                    })

                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "Tableau de bord",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    DashboardSection(
                        historyCount = uiState.historyCount,
                        cacheSize = uiState.cacheSizeMb,
                        onClearHistory = viewModel::clearHistory,
                        onTrafficClick = { onNavigateTo(AppDestinations.TRAFFIC) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        UtilityList(onItemClick = { utility ->
                            Toast.makeText(context, "TODO: ${utility.label}", Toast.LENGTH_SHORT)
                                .show()
                        })
                    }
                }
            }
        } else {
            // Mobile Layout (Single Column)
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
                    MainFeaturesGrid(onItemClick = ::onFeatureClick, columns = 2)
                }

                item {
                    Text(
                        text = "Services",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    ServicesRow(onItemClick = { service ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(service.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Erreur lien", Toast.LENGTH_SHORT).show()
                        }
                    })
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
                        onClearHistory = viewModel::clearHistory,
                        onTrafficClick = { onNavigateTo(AppDestinations.TRAFFIC) }
                    )
                }

                item {
                    Text(
                        text = "Application",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                    UtilityList(onItemClick = { utility ->
                        Toast.makeText(context, "TODO: ${utility.label}", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }
}

// --- Components ---

@Composable
fun MenuHeader(greeting: String, isCompact: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Taller header to make identity pop
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
        //.padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big App Identity
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(10.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "SuperBus",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(if (isCompact) 24.dp else 48.dp))

            // Greeting at the bottom left
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Start)
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Prêt à partir ?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FakeSearchBar(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp), // Fully rounded like Google Search
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Rechercher un arrêt, une ligne...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MainFeaturesGrid(onItemClick: (MenuFeature) -> Unit, columns: Int = 2) {
    Column(modifier = Modifier.padding(horizontal = if (columns == 2) 16.dp else 0.dp)) {
        // TOP 3 FEATURES: Map, Favorites, Lines (Icon only, Centered)
        val topFeatures = listOf(MenuFeature.MAP, MenuFeature.FAVORITES, MenuFeature.LINES)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topFeatures.forEach { feature ->
                CompactFeatureTile(
                    feature = feature,
                    onClick = { onItemClick(feature) },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f) // Square
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Remaining items: Calendar, Traffic
        val remainingFeatures = MenuFeature.entries.filter { !topFeatures.contains(it) }

        // Responsive Grid for remaining items (2 items per row max)
        remainingFeatures.chunked(2).forEachIndexed { index, rowFeatures ->
            if (index > 0) Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureTile(
                        feature = feature,
                        onClick = { onItemClick(feature) },
                        modifier = Modifier
                            .weight(1f)
                            .height(90.dp) // Standard tile height
                    )
                }
                // If odd number of items, add spacer to keep alignment
                if (rowFeatures.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CompactFeatureTile(
    feature: MenuFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (feature) {
        MenuFeature.MAP -> MaterialTheme.colorScheme.tertiary
        MenuFeature.FAVORITES -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (feature) {
        MenuFeature.MAP -> MaterialTheme.colorScheme.onTertiary
        MenuFeature.FAVORITES -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = feature.label, // Accessible label
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
fun FeatureTile(
    feature: MenuFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    iconSize: androidx.compose.ui.unit.Dp = 32.dp
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(iconSize)
            )
            Text(
                text = feature.label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.align(Alignment.BottomStart)
            )
        }
    }
}

@Composable
fun ServicesRow(onItemClick: (ExternalService) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ExternalService.entries.toList()) { service ->
            ServiceCard(service, onItemClick)
        }
    }
}

@Composable
fun ServicesGrid(onItemClick: (ExternalService) -> Unit) {
    // For Tablet: Display as a grid/flow
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExternalService.entries.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    ServiceCard(item, onItemClick, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ServiceCard(
    service: ExternalService,
    onClick: (ExternalService) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = { onClick(service) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(70.dp)
            .widthIn(min = 140.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = service.label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DashboardSection(
    historyCount: Int,
    cacheSize: Double,
    onClearHistory: () -> Unit,
    onTrafficClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
            onClick = {} // Just card
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Historique", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "$historyCount",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text("trajets récents", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onClearHistory,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Nettoyer")
                }
            }
        }

        Card(
            onClick = onTrafficClick,
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Traffic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trafic", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                    ) // Green dot
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Fluide",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Réseau normal", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun UtilityList(onItemClick: (UtilityItem) -> Unit) {
    Column {
        UtilityItem.entries.forEach { item ->
            ListItem(
                headlineContent = { Text(item.label) },
                leadingContent = {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.clickable { onItemClick(item) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

enum class MenuFeature(val label: String, val icon: ImageVector) {
    FAVORITES("Favoris", Icons.Default.Favorite),
    MAP("Carte", Icons.Default.Place),
    LINES("Lignes", Icons.Default.DirectionsBus),
    CALENDAR("Calendrier", Icons.Default.DateRange),
    TRAFFIC("Info Traffic", Icons.Default.Traffic)
}

enum class ExternalService(val label: String, val url: String, val icon: ImageVector) {
    VELOCITE("Vélocité", "https://www.velocite.fr", Icons.AutoMirrored.Filled.DirectionsBike),
    CITIZ("Citiz", "https://citiz.coop", Icons.Default.DirectionsCar),
    SNCF("SNCF", "https://www.sncf-connect.com", Icons.Default.Train),
    MOBIGO("Mobigo", "https://www.viamobigo.fr", Icons.Default.DirectionsBus)
}

enum class UtilityItem(val label: String, val icon: ImageVector) {
    SETTINGS("Paramètres", Icons.Default.Settings),
    ABOUT("A propos", Icons.Default.Info),
    HELP("Aide", Icons.AutoMirrored.Filled.HelpOutline)
}
