package tv.vizbee.screendemo.utils

class StringExt {
    companion object {
        fun String.camelCase(): String {
            return this.split(" ").joinToString("") { it.capitalize() }
        }
    }
}
