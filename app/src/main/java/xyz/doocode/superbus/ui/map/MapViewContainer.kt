package xyz.doocode.superbus.ui.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import xyz.doocode.superbus.core.dto.ginko.Arret
import xyz.doocode.superbus.core.dto.jcdecaux.Station
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt

@Composable
fun MapViewContainer(
    modifier: Modifier = Modifier,
    arrets: List<Arret>,
    veloStations: List<Station>,
    mapStyle: MapStyle = MapStyle.Streets,
    onArretClick: (Arret) -> Unit,
    onVelociteClick: (Station) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val mapView = remember {
        Configuration.getInstance()
            .load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        MapView(context).apply {
            setMultiTouchControls(true)
            controller.setZoom(MapConstants.DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(MapConstants.BESANCON_LAT, MapConstants.BESANCON_LON))
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(factory = { mapView }, modifier = modifier) { mv ->
        val tileSource = XYTileSource(mapStyle.id, 0, 19, 256, mapStyle.fileExt, mapStyle.baseUrls)
        mv.setTileSource(tileSource)

        // remove previous markers
        val toRemove = mv.overlays.filterIsInstance<Marker>().toList()
        toRemove.forEach { mv.overlays.remove(it) }

        // add Ginko stops
        arrets.forEach { a ->
            val marker = Marker(mv).apply {
                position = GeoPoint(a.latitude, a.longitude)
                title = a.nom
                subDescription = "ARRET|${a.id}"
                icon = createMarkerBitmap(
                    mv.context,
                    "#1E88E5".toColorInt()
                ).toDrawable(mv.context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            marker.setOnMarkerClickListener { _, _ ->
                onArretClick(a)
                true
            }
            mv.overlays.add(marker)
        }

        // add Vélocité stations
        veloStations.forEach { s ->
            val marker = Marker(mv).apply {
                position = GeoPoint(s.position.latitude, s.position.longitude)
                title = s.name
                subDescription = "VELO|${s.number}"
                icon = createMarkerBitmap(
                    mv.context,
                    "#43A047".toColorInt()
                ).toDrawable(mv.context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            marker.setOnMarkerClickListener { _, _ ->
                onVelociteClick(s)
                true
            }
            mv.overlays.add(marker)
        }

        mv.invalidate()
    }
}
