package xyz.doocode.superbus.ui.about

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import xyz.doocode.superbus.ui.theme.SuperBusTheme

class AboutActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SuperBusTheme {
                AboutScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Général", "Données & Licences")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("À propos") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> AboutGeneralContent(uriHandler)
                1 -> AboutDataLicenseContent(uriHandler)
            }
        }
    }
}

@Composable
fun AboutGeneralContent(uriHandler: UriHandler) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isLandscape = maxWidth > maxHeight
        val isWideScreen = maxWidth > 600.dp && isLandscape

        if (isWideScreen) {
            // Tablet / Landscape Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Logo & Branding
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AppIdentitySection()
                }

                // Right Column: Info Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    InfoCardsSection(uriHandler)
                    Spacer(modifier = Modifier.height(24.dp))
                    FooterSection()
                }
            }
        } else {
            // Mobile / Portrait Layout
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    AppIdentitySection()
                    Spacer(modifier = Modifier.height(48.dp))
                }

                item {
                    InfoCardsSection(uriHandler)
                    Spacer(modifier = Modifier.height(48.dp))
                    FooterSection()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun AppIdentitySection() {
    Box(contentAlignment = Alignment.Center) {
        var isIconRotated by remember { mutableStateOf(false) }
        val iconRotation by animateFloatAsState(
            targetValue = if (isIconRotated) 360f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "iconRotation"
        )

        // Background Gradient Blob
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                )
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(120.dp)
                .clickable { isIconRotated = !isIconRotated }
                .rotate(iconRotation)
                .shadow(elevation = 8.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Text(
        text = "SuperBus",
        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
        color = MaterialTheme.colorScheme.primary
    )
    Text(
        text = "v0.1.0-alpha",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.outline
    )
    Spacer(modifier = Modifier.height(8.dp))

    SuggestionChip(
        onClick = {},
        label = { Text("En cours de développement") },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        border = null
    )
}

@Composable
fun InfoCardsSection(uriHandler: UriHandler) {
    val lastUpdate = LocalDate.of(2026, 3, 24)
    val formattedDate = lastUpdate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

    AboutInfoCard(
        icon = Icons.Default.DateRange,
        label = "Mise à jour le",
        value = formattedDate,
        color = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer
    )
    Spacer(modifier = Modifier.height(2.dp))

    AboutInfoCard(
        icon = Icons.Default.Business,
        label = "Organisation",
        value = "Doocode.xyz",
        color = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = { uriHandler.openUri("https://doocode.xyz") },
        showLinkIcon = true
    )
    Spacer(modifier = Modifier.height(2.dp))

    AboutInfoCard(
        icon = Icons.Default.Code,
        label = "Développeur",
        value = "Aero15",
        color = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = { uriHandler.openUri("https://github.com/Aero15") },
        showLinkIcon = true
    )
    Spacer(modifier = Modifier.height(2.dp))

    AboutInfoCard(
        icon = Icons.Default.Gavel,
        label = "Licence",
        value = "MIT License",
        color = MaterialTheme.colorScheme.secondaryContainer,
        textColor = MaterialTheme.colorScheme.onSecondaryContainer,
        onClick = { uriHandler.openUri("https://opensource.org/licenses/MIT") },
        showLinkIcon = true
    )
}

@Composable
fun FooterSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Fait avec ❤️ à Besançon",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "© 2026 Doocode.xyz",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun AboutInfoCard(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    textColor: Color,
    onClick: (() -> Unit)? = null,
    showLinkIcon: Boolean = false
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (onClick != null) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = textColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = textColor.copy(alpha = 0.7f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
            if (showLinkIcon || onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Ouvrir le lien",
                    modifier = Modifier.size(20.dp),
                    tint = textColor.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun AboutDataLicenseContent(uriHandler: UriHandler) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Source des données",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Les données du réseau Ginko sont mises à disposition gratuitement en \"Open Data\".",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Text(
                text = "Licence et Utilisation",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Text(
                text = "Les données sont mises à disposition sous Licence ODbL (Open Database Licence). L'utilisation de l'API et/ou le téléchargement du jeu de données GTFS vaut acceptation de la licence.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Text(
                text = "Cette licence nous impose notamment de mentionner la provenance des informations utilisées par nos développements.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Conditions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Mentionner la paternité\n• Partage aux conditions identiques\n• Garder ouvert la base de données dérivée",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Text(
                text = "Liens utiles",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Button(
                    onClick = { uriHandler.openUri("https://ginko.voyage/") },
                    contentPadding = PaddingValues(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Site web Ginko Mobilités")
                }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://api.ginko.voyage") },
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Documentation API de Ginko")
                }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://opendatacommons.org/licenses/odbl/") },
                    contentPadding = PaddingValues(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Licence ODbL")
                }
            }
        }
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
fun AboutScreenPreview() {
    SuperBusTheme {
        AboutScreen(onBack = {})
    }
}
