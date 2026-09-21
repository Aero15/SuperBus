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
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
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
    onArretClick: (Arret) -> Unit,
    onVelociteClick: (Station) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val markersOverlay = remember { FolderOverlay() }

    val mapView = remember {
        Configuration.getInstance()
            .load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(MapConstants.DEFAULT_ZOOM)
            controller.setCenter(GeoPoint(MapConstants.BESANCON_LAT, MapConstants.BESANCON_LON))

            // Create an overlay to display/hide markers based on zoom level
            overlays.add(markersOverlay)
            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean = false
                override fun onZoom(event: ZoomEvent?): Boolean {
                    val isVisible = zoomLevelDouble >= MapConstants.MIN_ZOOM_MARKERS
                    if (markersOverlay.isEnabled != isVisible) {
                        markersOverlay.isEnabled = isVisible
                        postInvalidate()
                    }
                    return true
                }
            })
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
        // Clear existing markers from the folder overlay to avoid duplicates
        markersOverlay.items.clear()

        // add bus/tram stops
        arrets.forEach { a ->
            val marker = Marker(mv).apply {
                position = GeoPoint(a.latitude, a.longitude)
                title = a.nom
                subDescription = "ARRET|${a.id}"
                icon = createMarkerBitmap(
                    mv.context,
                    "#00abc4".toColorInt()
                ).toDrawable(mv.context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            marker.setOnMarkerClickListener { _, _ ->
                onArretClick(a)
                true
            }
            markersOverlay.add(marker)
        }

        // add Vélocité stations
        veloStations.forEach { s ->
            val marker = Marker(mv).apply {
                position = GeoPoint(s.position.latitude, s.position.longitude)
                title = s.name
                subDescription = "VELO|${s.number}"
                icon = createMarkerBitmap(
                    mv.context,
                    "#b7007a".toColorInt()
                ).toDrawable(mv.context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }
            marker.setOnMarkerClickListener { _, _ ->
                onVelociteClick(s)
                true
            }
            markersOverlay.add(marker)
        }

        markersOverlay.isEnabled = mv.zoomLevelDouble >= MapConstants.MIN_ZOOM_MARKERS
        mv.invalidate()
    }
}
