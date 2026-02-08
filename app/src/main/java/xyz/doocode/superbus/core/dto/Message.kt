package xyz.doocode.superbus.core.dto

data class Message(
    val id: Int,
    val titre: String,
    val texte: String,
    val url: String,
    val etat: Int,
    val idEvenement: Int,
    val lignes: List<String>,
    val arrets: List<String>,
    val supports: List<Int>
)
