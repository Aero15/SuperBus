package xyz.doocode.superbus.core.dto.ginko

data class FavoriteStation(
    val id: String,
    val name: String,
    val lines: List<Ligne>,
    val detailsFromId: Boolean,
    val kind: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val effectiveKind: String get() = kind ?: KIND_BUS_TRAM

    companion object {
        const val KIND_BUS_TRAM = "bus&tram"
        const val KIND_VELOCITE = "vélocité"
    }
}

