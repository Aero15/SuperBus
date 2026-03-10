package xyz.doocode.superbus.ui.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MenuFeature(val label: String, val icon: ImageVector) {
    FAVORITES("Favoris", Icons.Default.Favorite),
    MAP("Carte", Icons.Default.Map),
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
