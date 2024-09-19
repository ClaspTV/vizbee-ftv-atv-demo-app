package tv.vizbee.screendemo.auth

import android.content.Context
import tv.vizbee.screendemo.storage.SharedPreferencesManager

class AuthManager(context: Context) {
    private val sharedPreferencesManager = SharedPreferencesManager(context)

    fun isSignedIn(signInType: String): Boolean {
        val authToken = sharedPreferencesManager.getStringValue("authToken")
        return authToken.isNotEmpty()
    }
}