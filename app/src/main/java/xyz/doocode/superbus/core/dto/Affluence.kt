package xyz.doocode.superbus.core.dto

data class Affluence(
    val heureDeDebut: String,
    val heureDeFin: String,
    val affluence: Int,
    val texteAffluence: String,
    val nbPassagersABord: Int,
    val tauxDeCharge: Double
)
