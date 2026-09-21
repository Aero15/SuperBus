package xyz.doocode.superbus.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
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
    selectedLayer: MapLayer = MapLayer.STANDARD,
    trackUserLocation: Boolean = false,
    centerUserLocationTrigger: Int = 0,
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
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
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

    val locationHelper = remember(mapView) {
        UserLocationHelper(context, mapView)
    }

    DisposableEffect(lifecycleOwner, trackUserLocation) {
        if (trackUserLocation) {
            locationHelper.startListening()
        }
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView.onResume()
                    if (trackUserLocation) {
                        locationHelper.startListening()
                    }
                }

                Lifecycle.Event.ON_PAUSE -> {
                    locationHelper.stopListening()
                    mapView.onPause()
                }

                Lifecycle.Event.ON_DESTROY -> {
                    locationHelper.stopListening()
                    mapView.onDetach()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            locationHelper.stopListening()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(centerUserLocationTrigger) {
        if (centerUserLocationTrigger > 0) {
            locationHelper.centerOnUser(animate = true)
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier) { mv ->
        // Clear existing markers from the folder overlay to avoid duplicates
        markersOverlay.items.clear()

        // Add bus/tram stops according to selected layer
        if (selectedLayer == MapLayer.STANDARD || selectedLayer == MapLayer.BUS_TRAM) {
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
        }

        // Add Vélocité stations according to selected layer
        if (selectedLayer == MapLayer.STANDARD || selectedLayer == MapLayer.VELOCITE) {
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
        }

        markersOverlay.isEnabled = mv.zoomLevelDouble >= MapConstants.MIN_ZOOM_MARKERS
        mv.invalidate()
    }
}

private class UserLocationHelper(
    private val context: Context,
    private val mapView: MapView
) : LocationListener {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private var userMarker: Marker? = null
    private var isListening = false
    private var lastLocation: Location? = null
    private var pendingCenterAnimation = false

    fun startListening() {
        if (isListening || locationManager == null) return
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) return

        if (userMarker == null) {
            userMarker = Marker(mapView).apply {
                title = "Ma position"
                subDescription = "USER_LOCATION"
                icon = createUserLocationMarkerBitmap(context).toDrawable(context.resources)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            }
            mapView.overlays.add(userMarker)
        }

        try {
            val gpsLoc = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null
            val netLoc = if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            val bestLoc = when {
                gpsLoc != null && netLoc != null -> if (gpsLoc.time >= netLoc.time) gpsLoc else netLoc
                gpsLoc != null -> gpsLoc
                else -> netLoc
            }

            bestLoc?.let { loc ->
                lastLocation = loc
                val gp = GeoPoint(loc.latitude, loc.longitude)
                userMarker?.position = gp
                mapView.postInvalidate()
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    1000L,
                    1f,
                    this
                )
            }
            isListening = true
        } catch (_: SecurityException) {
        }
    }

    fun stopListening() {
        if (!isListening) return
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Throwable) {
        }
        isListening = false
    }

    fun centerOnUser(animate: Boolean = true) {
        val loc = lastLocation
        if (loc != null) {
            val gp = GeoPoint(loc.latitude, loc.longitude)
            if (animate) {
                mapView.controller.animateTo(
                    gp,
                    MapConstants.USER_LOCATION_ZOOM,
                    MapConstants.ANIMATION_DURATION_MS
                )
            } else {
                mapView.controller.setCenter(gp)
                mapView.controller.setZoom(MapConstants.USER_LOCATION_ZOOM)
            }
        } else {
            pendingCenterAnimation = true
            startListening()
        }
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
        val gp = GeoPoint(location.latitude, location.longitude)
        userMarker?.position = gp

        if (pendingCenterAnimation) {
            pendingCenterAnimation = false
            mapView.controller.animateTo(
                gp,
                MapConstants.USER_LOCATION_ZOOM,
                MapConstants.ANIMATION_DURATION_MS
            )
        }

        mapView.postInvalidate()
    }
}
