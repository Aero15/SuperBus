package xyz.doocode.superbus.core.dto

data class FavoriteStation(
    val id: String,
    val name: String,
    val lines: List<Ligne>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
