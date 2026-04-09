package xyz.doocode.superbus.core.dto.ginko

data class Arret(
    val id: String,
    val nom: String,
    val latitude: Double,
    val longitude: Double,
    val accessibilite: Int,
    // List of Arret grouped with this stop (for duplicates)
    // Not part of the API response, but used in the UI
    var duplicates: List<Arret> = emptyList()
) {
    val groupedIds: List<String>
        get() = duplicates.map { it.id }
}

