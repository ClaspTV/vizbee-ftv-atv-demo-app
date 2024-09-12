package tv.vizbee.screendemo

import android.content.Intent
import com.google.android.gms.cast.tv.CastReceiverContext

class CastUtil {
    companion object {
        fun handleIntentByCastReceiver(intent: Intent): Boolean {

            // ---------------------------
            // Begin SDK Integration
            // ---------------------------
            // if the SDK(Google Cast) recognizes the intent, we should early return.
            if (CastReceiverContext.getInstance() != null) {
                val mediaManager = CastReceiverContext.getInstance().mediaManager
                return mediaManager.onNewIntent(intent)
            }
            // ---------------------------
            // End SDK Integration
            // ---------------------------

            return false
        }
    }
}