package tv.vizbee.screendemo.utils

import java.util.Locale

fun String.camelCase(): String {
    return this.split(" ").joinToString("") { it.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }
}
