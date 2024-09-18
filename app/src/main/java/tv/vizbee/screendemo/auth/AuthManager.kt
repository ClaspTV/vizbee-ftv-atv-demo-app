package tv.vizbee.screendemo.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import tv.vizbee.screendemo.vizbee.homesso.MyVizbeeHomeSSOAdapter

class AuthManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)

    fun setSignedIn(signInType: String, isSignedIn: Boolean) {
        prefs.edit {
            putBoolean(signInType, isSignedIn)
        }
    }

    fun isSignedIn(signInType: String): Boolean {
        return prefs.getBoolean(signInType, false)
    }

    fun isSignedInAny(): Boolean {
        return MyVizbeeHomeSSOAdapter.SUPPORTED_SIGN_IN_TYPES.any { isSignedIn(it) }
    }
}