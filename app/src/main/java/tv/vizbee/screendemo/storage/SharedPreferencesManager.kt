package tv.vizbee.screendemo.storage

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("receiver-app", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    // Function to save a String value to SharedPreferences
    fun setStringValue(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    // Function to retrieve a String value from SharedPreferences
    fun getStringValue(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

}
