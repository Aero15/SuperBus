package xyz.doocode.superbus.ui.map

object MapConstants {
    const val BESANCON_LAT = 47.237829
    const val BESANCON_LON = 6.024053
    const val DEFAULT_ZOOM = 13.0
}

sealed class MapStyle(
    val id: String,
    val baseUrls: Array<String>,
    val fileExt: String
) {
    object Streets : MapStyle("osm_streets", arrayOf("https://tile.openstreetmap.org/"), ".png")
    object Toner : MapStyle("stamen_toner", arrayOf("http://tile.stamen.com/toner/"), ".png")
    object Satellite :
        MapStyle("satellite", arrayOf("https://api.maptiler.com/tiles/hybrid/"), ".jpg")
}
