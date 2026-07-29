package xyz.doocode.superbus.ui.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Explore
import androidx.compose.ui.graphics.vector.ImageVector

enum class StopDetailsTab(val title: String, val icon: ImageVector) {
    SCHEDULES("Bus et tram", Icons.Default.DirectionsBus),
    NEARBY("À proximité", Icons.Default.Explore),
    VELOCITE("Vélocité", Icons.AutoMirrored.Filled.DirectionsBike);

    companion object {
        fun buildTabs(hasVelocite: Boolean): List<StopDetailsTab> {
            return mutableListOf(SCHEDULES, NEARBY).apply {
                if (hasVelocite) add(VELOCITE)
            }
        }
    }
}
