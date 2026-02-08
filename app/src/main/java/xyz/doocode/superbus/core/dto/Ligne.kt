package xyz.doocode.superbus.core.dto

data class Variante(
    val id: String,
    val destination: String,
    val precisionDestination: String,
    val sensAller: Boolean
)

data class Ligne(
    val id: String,
    val numLignePublic: String,
    val libellePublic: String,
    val couleurFond: String,
    val couleurTexte: String,
    val modeTransport: Int,
    val typologie: Int,
    val scolaire: Boolean,
    val periurbain: Boolean,
    val variantes: List<Variante>
)

data class EtatLigne(
    val idLigne: String,
    val numLignePublic: String,
    val couleurFond: String,
    val couleurTexte: String,
    val etat: Int
)
