package xyz.doocode.superbus.ui.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.vector.ImageVector
import xyz.doocode.superbus.core.dto.jcdecaux.Station

enum class MapLayer(val label: String, val description: String) {
    STANDARD("Standard", "Arrêts de bus, tram et stations Vélocité"),
    BUS_TRAM("Bus & tram", "Arrêts de bus et tram"),
    VELOCITE("Vélocité", "Stations Vélocité")
}

enum class VelociteMapDisplayMode(
    val label: String,
    val description: String,
    val icon: ImageVector
) {
    BASIC("Simple", "Affichage standard des stations", Icons.Default.Info),
    AVAILABLE_BIKES(
        "Vélos disponibles",
        "Nombre de vélos disponibles",
        Icons.AutoMirrored.Filled.DirectionsBike
    ),
    AVAILABLE_STANDS(
        "Places disponibles",
        "Nombre de places disponibles",
        Icons.Filled.LocalParking
    ),
    MECHANICAL_BIKES(
        "Vélos mécaniques",
        "Nombre de vélos mécaniques disponibles",
        Icons.Filled.PedalBike
    ),
    ELECTRICAL_BIKES(
        "Vélos électriques",
        "Nombre de vélos électriques disponibles",
        Icons.Filled.ElectricBike
    ),
    UNAVAILABLE_STANDS(
        "Bornes indisponibles",
        "Nombre de bornes indisponibles",
        Icons.Filled.Warning
    ),
    CAPACITY("Capacité", "Capacité totale de chaque station", Icons.Filled.Storage),
    NUMBER("Numéro", "Numéro de chaque station", Icons.Filled.Tag),
    OPEN("Ouverte", "Distingue les stations ouvertes et fermées", Icons.Filled.CheckCircle),
    CONNECTED("En ligne", "Distingue les stations connectées et déconnectées", Icons.Filled.Wifi),
    BONUS("Bonus", "Distingue les stations offrant des points bonus", Icons.Filled.AutoAwesome),
    BANKING(
        "Paiement CB",
        "Indique si le paiement par carte bancaire est possible",
        Icons.Filled.CreditCard
    )
}

data class VelociteMarkerData(
    val text: String?,
    val color: Int,
    val infoText: String
)

fun getVelociteMarkerData(station: Station, mode: VelociteMapDisplayMode): VelociteMarkerData {
    val unavailableStands = maxOf(
        0,
        station.totalStands.capacity -
                (
                        station.mainStands.availabilities.mechanicalBikes +
                                station.mainStands.availabilities.electricalBikes +
                                station.mainStands.availabilities.stands
                        )
    )

    val orangeColor = 0xFFFF9800.toInt()
    val greenColor = 0xFF4CAF50.toInt()
    val redColor = 0xFFD32F2F.toInt()
    val blueColor = 0xFF00AAC2.toInt()
    val magentaColor = 0xFFB7007A.toInt()

    fun countColor(count: Int): Int = when {
        count == 0 -> redColor
        count <= 2 -> orangeColor
        else -> greenColor
    }

    return when (mode) {
        VelociteMapDisplayMode.BASIC -> {
            VelociteMarkerData(
                text = null,
                color = magentaColor,
                infoText = ""
            )
        }

        VelociteMapDisplayMode.AVAILABLE_BIKES -> {
            val count = station.mainStands.availabilities.bikes
            VelociteMarkerData(
                text = "$count",
                color = countColor(count),
                infoText = "$count vélo(s) disponibles"
            )
        }

        VelociteMapDisplayMode.AVAILABLE_STANDS -> {
            val count = station.mainStands.availabilities.stands
            VelociteMarkerData(
                text = "$count",
                color = countColor(count),
                infoText = "$count place(s) disponibles"
            )
        }

        VelociteMapDisplayMode.MECHANICAL_BIKES -> {
            val count = station.mainStands.availabilities.mechanicalBikes
            VelociteMarkerData(
                text = "$count",
                color = countColor(count),
                infoText = "$count vélo(s) mécanique(s)"
            )
        }

        VelociteMapDisplayMode.ELECTRICAL_BIKES -> {
            val count = station.mainStands.availabilities.electricalBikes
            VelociteMarkerData(
                text = "$count",
                color = countColor(count),
                infoText = "$count vélo(s) électrique(s)"
            )
        }

        VelociteMapDisplayMode.UNAVAILABLE_STANDS -> {
            val color = when {
                unavailableStands == station.totalStands.capacity && station.totalStands.capacity > 0 -> redColor
                unavailableStands > 0 -> orangeColor
                else -> greenColor
            }
            VelociteMarkerData(
                text = "$unavailableStands",
                color = color,
                infoText = "$unavailableStands borne(s) indisponibles"
            )
        }

        VelociteMapDisplayMode.CAPACITY -> {
            val capacity = station.totalStands.capacity
            VelociteMarkerData(
                text = "$capacity",
                color = blueColor,
                infoText = "Capacité : $capacity"
            )
        }

        VelociteMapDisplayMode.NUMBER -> {
            VelociteMarkerData(
                text = "${station.number}",
                color = magentaColor,
                infoText = "Station #${station.number}"
            )
        }

        VelociteMapDisplayMode.OPEN -> {
            val isOpen = station.status == "OPEN"
            VelociteMarkerData(
                text = if (isOpen) "✓" else "✕",
                color = if (isOpen) greenColor else redColor,
                infoText = if (isOpen) "Ouverte" else "Fermée"
            )
        }

        VelociteMapDisplayMode.CONNECTED -> {
            val isConnected = station.connected
            VelociteMarkerData(
                text = if (isConnected) "✓" else "✕",
                color = if (isConnected) greenColor else redColor,
                infoText = if (isConnected) "Connectée" else "Hors-ligne"
            )
        }

        VelociteMapDisplayMode.BONUS -> {
            val isBonus = station.bonus
            VelociteMarkerData(
                text = if (isBonus) "★" else "•",
                color = if (isBonus) orangeColor else 0xFF757575.toInt(),
                infoText = if (isBonus) "Bonus" else "Non bonus"
            )
        }

        VelociteMapDisplayMode.BANKING -> {
            val hasBanking = station.banking
            VelociteMarkerData(
                text = if (hasBanking) "CB" else "✕",
                color = if (hasBanking) greenColor else 0xFF757575.toInt(),
                infoText = if (hasBanking) "Paiement CB disponible" else "Sans terminal de paiement"
            )
        }
    }
}
