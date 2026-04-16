package xyz.doocode.superbus.ui.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class MenuFeature(val label: String, val icon: ImageVector) {
    FAVORITES("Favoris", Icons.Default.Favorite),

    //MAP("Carte", Icons.Default.Map),
    VELOCITE("Vélocité", Icons.AutoMirrored.Filled.DirectionsBike),
    LINES("Lignes", Icons.Default.DirectionsBus),
    CALENDAR("Calendrier", Icons.Default.DateRange),
    TRAFFIC("Info Traffic", Icons.Default.Traffic)
}

enum class ExternalService(val label: String, val url: String, val icon: ImageVector) {
    VELOCITE(
        "Vélocité",
        "https://velocite.ginko.voyage/",
        Icons.AutoMirrored.Filled.DirectionsBike
    ),
    CITIZ("Citiz", "https://bfc.citiz.coop/", Icons.Default.DirectionsCar),
    MOBIGO("Mobigo", "https://www.viamobigo.fr", Icons.Default.DirectionsBus),
    SNCF("SNCF", "https://www.sncf-connect.com", Icons.Default.Train)
}

enum class UtilityItem(val label: String, val icon: ImageVector) {
    SETTINGS("Paramètres", Icons.Default.Settings),
    ABOUT("A propos", Icons.Default.Info),
    HELP("Aide", Icons.AutoMirrored.Filled.HelpOutline)
}
