package xyz.doocode.superbus.core.dto

data class Arret(
    val id: String,
    val nom: String,
    val latitude: Double,
    val longitude: Double,
    val accessibilite: Int,
    // List of IDs grouped with this stop (for duplicates)
    // Not part of the API response, but used in the UI
    var groupedIds: List<String> = emptyList()
)
