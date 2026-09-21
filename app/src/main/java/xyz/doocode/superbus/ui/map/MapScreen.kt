package xyz.doocode.superbus.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
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

        if (loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
