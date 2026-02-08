package xyz.doocode.superbus.core.dto

data class LineInfo(
    val numLigne: String,
    val couleurFond: String,
    val couleurTexte: String
)

data class FavoriteStation(
    val id: String,
    val name: String,
    val lines: List<LineInfo>
)
