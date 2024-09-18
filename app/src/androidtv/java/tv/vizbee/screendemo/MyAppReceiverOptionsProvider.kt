package tv.vizbee.screendemo

import android.content.Context
import com.google.android.gms.cast.tv.CastReceiverOptions
import com.google.android.gms.cast.tv.ReceiverOptionsProvider

class MyAppReceiverOptionsProvider : ReceiverOptionsProvider {
    override fun getOptions(context: Context): CastReceiverOptions {
        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        return CastReceiverOptions.Builder(context)
            .setStatusText("My App")
            .setCustomNamespaces(listOf("urn:x-cast:tv.vizbee.sync"))
            .build()
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------
    }
}