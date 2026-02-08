package xyz.doocode.superbus.core.dto

data class Temps(
    val idArret: String,
    val latitude: Double,
    val longitude: Double,
    val idLigne: String,
    val numLignePublic: String,
    val couleurFond: String,
    val couleurTexte: String,
    val sensAller: Boolean,
    val destination: String,
    val precisionDestination: String,
    val temps: String,
    val tempsHTML: String,
    val tempsEnSeconde: Int,
    val typeDeTemps: Int,
    val alternance: Boolean,
    val tempsHTMLEnAlternance: String,
    val fiable: Boolean,
    val numVehicule: String,
    val accessibiliteArret: Int,
    val accessibiliteVehicule: Int,
    val affluence: Int,
    val texteAffluence: String,
    val aideDecisionAffluence: String,
    val tauxDeCharge: Double,
    val idInfoTrafic: Int,
    val modeTransport: Int
)

data class TempsLieu(
    val nomExact: String,
    val listeTemps: List<Temps>,
    val latitude: Double,
    val longitude: Double
)
