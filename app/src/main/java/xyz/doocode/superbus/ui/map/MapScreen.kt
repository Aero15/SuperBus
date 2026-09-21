package xyz.doocode.superbus.ui.map

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.doocode.superbus.core.data.ReferenceDataRepository
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import xyz.doocode.superbus.ui.details.StopDetailsActivity
import xyz.doocode.superbus.ui.details.velocite.VelociteDetailsActivity

@PreviewScreenSizes
@Composable
fun MapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var arrets by remember { mutableStateOf<List<Arret>>(emptyList()) }
    var velos by remember { mutableStateOf<List<Station>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    var selectedLayer by remember { mutableStateOf(MapLayer.STANDARD) }
    var showLayerSheet by remember { mutableStateOf(false) }

    var isTrackingLocation by remember { mutableStateOf(false) }
    var centerTrigger by remember { mutableIntStateOf(0) }
    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        permissionGranted = granted
        if (granted) {
            isTrackingLocation = true
            centerTrigger++
        }
    }

    LaunchedEffect(Unit) {
        try {
            val repo = ReferenceDataRepository.getInstance(context)
            withContext(Dispatchers.IO) {
                val a = repo.getArrets()
                val v = repo.getVelociteStations()
                withContext(Dispatchers.Main) {
                    arrets = a
                    velos = v
                    loading = false
                }
            }
        } catch (t: Throwable) {
            loading = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapViewContainer(
            modifier = Modifier.fillMaxSize(),
            arrets = arrets,
            veloStations = velos,
            selectedLayer = selectedLayer,
            trackUserLocation = isTrackingLocation || permissionGranted,
            centerUserLocationTrigger = centerTrigger,
            onArretClick = { arret ->
                val intent =
                    android.content.Intent(context, StopDetailsActivity::class.java).apply {
                        putExtra(StopDetailsActivity.EXTRA_STOP_ID, arret.id)
                        putExtra(StopDetailsActivity.EXTRA_STOP_NAME, arret.nom)
                        putExtra(StopDetailsActivity.EXTRA_DETAILS_FROM_ID, true)
                    }
                context.startActivity(intent)
            },
            onVelociteClick = { station ->
                val intent =
                    android.content.Intent(context, VelociteDetailsActivity::class.java).apply {
                        putExtra(VelociteDetailsActivity.EXTRA_STATION_ID, station.number)
                        putExtra(VelociteDetailsActivity.EXTRA_STATION_NAME, station.name)
                    }
                context.startActivity(intent)
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = { showLayerSheet = true }
            ) {
                Icon(
                    imageVector = Icons.Filled.Layers,
                    contentDescription = "Changer de calque"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = {
                    if (permissionGranted) {
                        isTrackingLocation = true
                        centerTrigger++
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Ma position"
                )
            }
        }

        if (showLayerSheet) {
            MapLayersBottomSheet(
                selectedLayer = selectedLayer,
                onLayerSelected = { selectedLayer = it },
                onDismissRequest = { showLayerSheet = false }
            )
        }

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
