package tv.vizbee.screendemo.utils

import java.util.Locale

fun String.camelCase(): String {
    return this.split(" ").joinToString("") { it.capitalize(Locale.getDefault()) }
}
