package xyz.doocode.superbus.core.dto

data class FavoriteStation(
    val id: String,
    val groupedIds: List<String>? = emptyList(), // Store grouped IDs here (nullable for Gson safety)
    val name: String,
    val lines: List<Ligne>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
