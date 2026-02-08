package xyz.doocode.superbus.core.dto

data class Arret(
    val id: String,
    val nom: String,
    val latitude: Double,
    val longitude: Double,
    val accessibilite: Int
)
