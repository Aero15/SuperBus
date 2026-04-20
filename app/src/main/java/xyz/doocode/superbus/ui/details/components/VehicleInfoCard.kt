package xyz.doocode.superbus.ui.details.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Accessible
import androidx.compose.material.icons.automirrored.filled.Dvr
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.doocode.superbus.core.dto.ginko.VehiculeDR
import xyz.doocode.superbus.ui.theme.SuperBusTheme

private enum class EquipmentStatus {
    Available,
    Unavailable,
    Unknown
}

@Composable
fun VehicleInfoCard(
    vehicleNumber: String?,
    vehicle: VehiculeDR?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    fullBleed: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = if (fullBleed) RectangleShape else RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (fullBleed) 20.dp else 16.dp,
                    vertical = 20.dp
                ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            when {
                isLoading -> {
                    VehicleSectionHeader(
                        title = "Infos du véhicule",
                        icon = Icons.Default.DirectionsBus
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                vehicle == null -> {
                    VehicleSectionHeader(
                        title = "Infos du véhicule",
                        icon = Icons.Default.Info
                    )

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    ) {
                        Text(
                            text = if (vehicleNumber.isNullOrBlank()) {
                                "Aucune information disponible pour ce véhicule"
                            } else {
                                "Aucune information disponible pour le véhicule n°$vehicleNumber"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 24.dp)
                        )
                    }
                }

                else -> {
                    VehicleSectionHeader(
                        title = "Infos du véhicule",
                        icon = Icons.Default.DirectionsBus
                    )

                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Column(
                                        modifier = Modifier.padding(
                                            horizontal = 18.dp,
                                            vertical = 14.dp
                                        ),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Numéro",
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = vehicle.num.ifBlank { "?" },
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Type de véhicule",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = vehicle.typeVehicule.ifBlank { "Information non disponible" },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = energyIcon(vehicle.energie),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Column {
                                    Text(
                                        text = "Type d'énergie",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = energyLabel(vehicle.energie),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    VehicleSectionHeader(
                        title = "Équipements du véhicule",
                        icon = Icons.Default.AutoAwesome
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EquipmentBubble(
                                title = accessibilityLabel(vehicle.accessiblite),
                                icon = Icons.AutoMirrored.Filled.Accessible,
                                status = when (vehicle.accessiblite) {
                                    1 -> EquipmentStatus.Available
                                    2 -> EquipmentStatus.Unavailable
                                    else -> EquipmentStatus.Unknown
                                },
                                modifier = Modifier.weight(1f)
                            )
                            EquipmentBubble(
                                title = "Climatisation",
                                icon = Icons.Default.AcUnit,
                                status = if (vehicle.climatisation) EquipmentStatus.Available else EquipmentStatus.Unavailable,
                                modifier = Modifier.weight(1f)
                            )
                            EquipmentBubble(
                                title = "Prises USB",
                                icon = Icons.Default.Power,
                                status = if (vehicle.prisesUSB) EquipmentStatus.Available else EquipmentStatus.Unavailable,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EquipmentBubble(
                                title = "Affichage dynamique",
                                icon = Icons.AutoMirrored.Filled.Dvr,
                                status = if (vehicle.affichageDynamique) EquipmentStatus.Available else EquipmentStatus.Unavailable,
                                modifier = Modifier.weight(1f)
                            )
                            EquipmentBubble(
                                title = "Annonces sonores",
                                icon = Icons.Default.Campaign,
                                status = if (vehicle.annoncesSonores) EquipmentStatus.Available else EquipmentStatus.Unavailable,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VehicleSectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ) {
            Box(
                modifier = Modifier.padding(9.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EquipmentBubble(
    title: String,
    icon: ImageVector,
    status: EquipmentStatus,
    modifier: Modifier = Modifier
) {
    val containerColor = MaterialTheme.colorScheme.surfaceVariant
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val badgeColor = when (status) {
        EquipmentStatus.Available -> Color(0xFF4CAF50)
        EquipmentStatus.Unavailable -> Color(0xFFE53935)
        EquipmentStatus.Unknown -> Color(0xFF757575)
    }
    val badgeIcon = when (status) {
        EquipmentStatus.Available -> Icons.Default.Check
        EquipmentStatus.Unavailable -> Icons.Default.Close
        EquipmentStatus.Unknown -> Icons.Default.Info
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(90.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = containerColor,
                tonalElevation = 0.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier
                            .size(38.dp)
                            .then(
                                if (status == EquipmentStatus.Unavailable) Modifier.drawWithContent {
                                    drawContent()
                                    val strokeWidth = 5.dp.toPx()
                                    drawLine(
                                        color = badgeColor,
                                        start = Offset(4f, size.height - 4f),
                                        end = Offset(size.width - 4f, 4f),
                                        strokeWidth = strokeWidth,
                                        cap = StrokeCap.Round
                                    )
                                } else Modifier
                            )
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(28.dp),
                shape = CircleShape,
                color = badgeColor,
                contentColor = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            color = contentColor,
            maxLines = 2
        )
    }
}

private fun energyLabel(energie: Int): String = when (energie) {
    1 -> "Gazole"
    2 -> "Gaz naturel"
    3 -> "Hybride"
    4 -> "Électrique"
    else -> "Info non disponible"
}

private fun energyIcon(energie: Int): ImageVector = when (energie) {
    1, 2 -> Icons.Default.LocalGasStation
    3, 4 -> Icons.Default.Bolt
    else -> Icons.Default.Info
}

private fun accessibilityLabel(accessibility: Int): String = when (accessibility) {
    1 -> "Accessible aux PMR"
    2 -> "Non accessible aux PMR"
    else -> "Info indisponible"
}

@Preview(showBackground = true)
@Composable
private fun VehicleInfoCardPreview() {
    SuperBusTheme {
        VehicleInfoCard(
            vehicleNumber = "812",
            isLoading = false,
            vehicle = VehiculeDR(
                num = "812",
                affichageDynamique = true,
                annoncesSonores = true,
                climatisation = false,
                prisesUSB = true,
                accessiblite = 1,
                energie = 4,
                typeVehicule = "Bus Standard"
            )
        )
    }
}
