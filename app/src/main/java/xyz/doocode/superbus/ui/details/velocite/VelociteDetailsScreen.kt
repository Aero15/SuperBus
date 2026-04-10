package xyz.doocode.superbus.ui.details.velocite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.components.ErrorView
import xyz.doocode.superbus.ui.components.LoadingView
import xyz.doocode.superbus.ui.theme.AvailableStandsYellow
import xyz.doocode.superbus.ui.theme.ElectricBikeGreen
import xyz.doocode.superbus.ui.theme.MechanicalBikeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VelociteDetailsScreen(
    stationName: String,
    viewModel: VelociteDetailsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val formattedName =
        stationName
            .replace(Regex("^\\d+\\s*-\\s*"), "")
            .replace(" (CB)", "")
            .lowercase()
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = formattedName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor =
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is VelociteDetailsUiState.Loading -> {
                    LoadingView()
                }

                is VelociteDetailsUiState.Error -> {
                    ErrorView(message = state.message, onRetry = { viewModel.reload() })
                }

                is VelociteDetailsUiState.Success -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        VelociteRecap(station = state.station)

                        CapacityChartCard(station = state.station)

                        StatusCard(station = state.station)

                        AddressCard(station = state.station)
                    }
                }
            }
        }
    }
}

@Composable
fun AddressCard(station: Station) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Station #${station.number}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Adresse",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = station.address, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun CapacityChartCard(station: Station) {
    val totalBikes = station.totalStands.availabilities.bikes
    val availableStands = station.totalStands.availabilities.stands
    val mechBikes = station.totalStands.availabilities.mechanicalBikes
    val elecBikes = station.totalStands.availabilities.electricalBikes
    val capacity = station.totalStands.capacity
    val unavailableStands = maxOf(0, capacity - (mechBikes + elecBikes + availableStands))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Répartition détaillée",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text =
                    "Visualisez la capacité de la station, ainsi que les bornes hors service ou mal enclenchées.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (capacity > 0) {
                val sum = mechBikes + elecBikes + availableStands + unavailableStands
                val wMech = mechBikes.toFloat()
                val wElec = elecBikes.toFloat()
                val wAvail = availableStands.toFloat()
                val wUnavail = unavailableStands.toFloat()

                // Bar chart
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (wMech > 0) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(wMech)
                                    .fillMaxHeight()
                                    .background(MechanicalBikeBlue)
                        )
                    }
                    if (wElec > 0) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(wElec)
                                    .fillMaxHeight()
                                    .background(ElectricBikeGreen)
                        )
                    }
                    if (wAvail > 0) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(wAvail)
                                    .fillMaxHeight()
                                    .background(AvailableStandsYellow)
                        )
                    }
                    if (wUnavail > 0) {
                        Box(
                            modifier =
                                Modifier
                                    .weight(wUnavail)
                                    .fillMaxHeight()
                                    .background(Color(0xFFE53935))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LegendItem(
                        color = MechanicalBikeBlue,
                        label = "Vélos mécaniques",
                        value = mechBikes
                    )
                    LegendItem(
                        color = ElectricBikeGreen,
                        label = "Vélos électriques",
                        value = elecBikes
                    )
                    LegendItem(
                        color = AvailableStandsYellow,
                        label = "Places disponibles",
                        value = availableStands
                    )
                    LegendItem(
                        color = Color(0xFFE53935),
                        label = "Hors service / Non dispo.",
                        value = unavailableStands
                    )
                }
            } else {
                Text(text = "Capacité inconnue.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StatusCard(station: Station) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Informations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusItem(
                    icon =
                        if (station.status == "OPEN") Icons.Default.CheckCircle
                        else Icons.Default.Cancel,
                    color = if (station.status == "OPEN") ElectricBikeGreen else Color.Red,
                    label = if (station.status == "OPEN") "Ouverte" else "Fermée"
                )

                StatusItem(
                    icon = if (station.connected) Icons.Default.Wifi else Icons.Default.WifiOff,
                    color =
                        if (station.connected) MaterialTheme.colorScheme.primary
                        else Color.Gray,
                    label = if (station.connected) "En Ligne" else "Déconnectée"
                )

                StatusItem(
                    icon = Icons.Default.CreditCard,
                    color =
                        if (station.banking) MaterialTheme.colorScheme.primary
                        else Color.Gray,
                    label = "CB",
                    strikeThrough = !station.banking
                )

                StatusItem(
                    icon = Icons.Default.Star,
                    color = if (station.bonus) AvailableStandsYellow else Color.Gray,
                    label = "Bonus",
                    strikeThrough = !station.bonus
                )
            }
        }
    }
}

@Composable
fun StatusItem(icon: ImageVector, color: Color, label: String, strikeThrough: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            textDecoration =
                if (strikeThrough) androidx.compose.ui.text.style.TextDecoration.LineThrough
                else null
        )
    }
}

@Composable
fun VelociteRecap(station: Station) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Mechanical bikes tile
        RecapTile(
            icon = Icons.Filled.PedalBike,
            label = "Vélos\nmécanique",
            value = station.totalStands.availabilities.mechanicalBikes.toString(),
            backgroundColor = MechanicalBikeBlue,
            modifier = Modifier.weight(1f)
        )

        // Electric bikes tile
        RecapTile(
            icon = Icons.Filled.ElectricBike,
            label = "Vélos\nélectrique",
            value = station.totalStands.availabilities.electricalBikes.toString(),
            backgroundColor = ElectricBikeGreen,
            modifier = Modifier.weight(1f)
        )

        // Available stands tile
        RecapTile(
            icon = Icons.Filled.LocalParking,
            label = "Places\ndispo",
            value = station.totalStands.availabilities.stands.toString(),
            backgroundColor = AvailableStandsYellow,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RecapTile(
    icon: ImageVector,
    label: String,
    value: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val isZero = value.toIntOrNull() == 0

    // Définition des couleurs pour un rendu "Sublime"
    val displayContentColor = if (isZero) MaterialTheme.colorScheme.error else Color.White
    val baseColor = if (isZero) MaterialTheme.colorScheme.surfaceVariant else backgroundColor

    // Création d'un dégradé pour la profondeur
    val backgroundBrush =
        if (isZero) {
            Brush.linearGradient(
                colors = listOf(baseColor.copy(alpha = 0.5f), baseColor.copy(alpha = 0.3f))
            )
        } else {
            Brush.linearGradient(
                colors =
                    listOf(
                        baseColor,
                        baseColor.copy(
                            alpha = 0.75f
                        ) // Légère variation pour l'effet de lumière
                    ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        }

    Card(
        modifier = modifier.height(115.dp), // Hauteur augmentée pour le style carte
        shape = RoundedCornerShape(16.dp), // Coins très arrondis
        elevation = CardDefaults.cardElevation(defaultElevation = if (isZero) 0.dp else 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            // Icône en filigrane (Watermark) décoratif
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = displayContentColor.copy(alpha = if (isZero) 0.1f else 0.2f),
                modifier =
                    Modifier
                        .size(90.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 24.dp, y = -24.dp) // Débordement intentionnel
                        .rotate(-15f)
            )

            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // Valeur (Chiffre) - Mise en avant majeure
                Text(
                    text = value,
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            // Ombre portée légère sur le texte pour lisibilité si fond
                            // clair (rare ici)
                            shadow =
                                androidx.compose.ui.graphics.Shadow(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                )
                        ),
                    color = displayContentColor
                )

                // Libellé
                Text(
                    text = label.uppercase(), // Majuscules pour un look plus technique/propre
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                    color = displayContentColor.copy(alpha = 0.9f),
                    lineHeight = 12.sp
                )
            }
        }
    }
}
