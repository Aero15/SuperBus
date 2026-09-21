package xyz.doocode.superbus.ui.map

enum class MapLayer(val label: String, val description: String) {
    STANDARD("Standard", "Arrêts de bus, tram et stations Vélocité"),
    BUS_TRAM("Bus & tram", "Arrêts de bus et tram"),
    VELOCITE("Vélocité", "Stations Vélocité")
}
