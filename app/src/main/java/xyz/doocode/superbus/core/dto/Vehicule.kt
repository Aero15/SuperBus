package xyz.doocode.superbus.core.dto

data class VehiculeDR(
    val num: String,
    val affichageDynamique: Boolean,
    val annoncesSonores: Boolean,
    val climatisation: Boolean,
    val prisesUSB: Boolean,
    val accessiblite: Int,
    val energie: Int,
    val typeVehicule: String
)

data class VehiculeTR(
    val num: String,
    val affluence: Int,
    val texteAffluence: String,
    val nbPassagersABord: Int,
    val tauxDeCharge: Double,
    val position: String
)
