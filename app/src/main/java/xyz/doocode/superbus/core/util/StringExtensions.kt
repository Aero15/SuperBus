package xyz.doocode.superbus.core.util

import java.text.Normalizer
import java.util.regex.Pattern

fun String.removeAccents(): String {
    val nfdNormalizedString = Normalizer.normalize(this, Normalizer.Form.NFD)
    val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
    return pattern.matcher(nfdNormalizedString).replaceAll("")
}
