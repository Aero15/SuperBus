package xyz.doocode.superbus.ui.details.velocite.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.jcdecaux.Station

@Composable
fun VelociteDetailsContent(
    station: Station,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable { onClick() }
                    else Modifier
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VelociteRecap(station = station, expanded = true, contentPadding = horizontalPadding)
            VelociteCapacityChartCard(station = station, horizontalPadding = horizontalPadding)
        }

        VelociteStatusCard(station = station, horizontalPadding = horizontalPadding)
        VelociteAddressCard(station = station, horizontalPadding = horizontalPadding)
    }
}
