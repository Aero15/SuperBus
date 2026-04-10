package xyz.doocode.superbus.core.util

fun formatVelociteStationName(rawName: String): String {
    return rawName.replaceFirst(Regex("^\\d+\\s*-\\s*"), "")
        .replace(" (CB)", "")
        .lowercase()
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
