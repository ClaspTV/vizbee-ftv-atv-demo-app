package tv.vizbee.screendemo.vizbee

import android.app.Application
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screen.api.adapter.VizbeeAppAdapter
import tv.vizbee.screen.api.adapter.VizbeePlayerAdapter
import tv.vizbee.screendemo.model.Video
import tv.vizbee.screendemo.vizbee.applifecycle.AppReadyModel
import tv.vizbee.screendemo.vizbee.applifecycle.MyVizbeeAppLifecycleAdapter
import tv.vizbee.screendemo.vizbee.video.adapter.MyVizbeeMediaConverter
import tv.vizbee.screendemo.vizbee.video.adapter.MyVizbeeMediaSessionCompatPlayerAdapter
import tv.vizbee.screendemo.vizbee.video.adapter.MyVizbeePlayerAdapterHandler
import tv.vizbee.screendemo.vizbee.video.deeplink.MyVizbeeAppAdapter

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This class is an entry point to the Vizbee integration. It has a utility method to initialise the
 * Vizbee SDK: the Continuity SDK. It has methods to know if Vizbee is enabled
 * by the app and some easy to access adapter objects via extension methods.
 */
object VizbeeWrapper {

    private var isVizbeeEnabled: Boolean = false
    val appLifecycleAdapter by lazy {
        MyVizbeeAppLifecycleAdapter()
    }
    private val playerAdapterHandler by lazy {
        MyVizbeePlayerAdapterHandler(isVizbeeEnabled)
    }

    //------
    // Initialisation
    //------

    /**
     * Initializes Vizbee SDK with the app ID assigned for your app.
     *
     * Invoke this method in the onCreate()
     * lifecycle callback of your Application.
     *
     * @param app Application
     */
    fun initialize(app: Application) {

        isVizbeeEnabled = isVizbeeEnabled(app)
        if (!isVizbeeEnabled) {
            Log.i(
                "VizbeeWrapper",
                "Vizbee is not enabled. Not initialising. Define your_app_vizbee_app_id for app to enable Vizbee."
            )
            return
        }
        val appAdapter: VizbeeAppAdapter = MyVizbeeAppAdapter(app, appLifecycleAdapter)
        Vizbee.getInstance().enableVerboseLogging()

        // Initialise Vizbee Continuity SDK
        Vizbee.getInstance().initialize(app, getVizbeeAppId(app), appAdapter)
    }

    //------
    // Helpers
    //------

    /**
     * This method returns true if Vizbee is enabled by the app. Returning false will disable all the Vizbee integration.
     *
     * @return true if Vizbee should be enabled in the app integration.
     */
    private fun isVizbeeEnabled(context: Context): Boolean {
        val vizbeeAppId = getVizbeeAppId(context)
        return vizbeeAppId.isNotEmpty()
    }

    /**
     * This method returns the vizbee_app_id defined the resources.
     *
     * #VizbeeGuide For best practices, define the resource in Gradle under product flavors to be able to change
     * it specific to a build variant.
     *
     * android {
     *      productFlavors {
     *          prod {
     *              ...
     *              resValue "string", "vizbee_app_id", "vzb********"
     *          }
     *      }
     * }
     *
     * @return returns the vizbee_app_id defined the resources.
     */
    private fun getVizbeeAppId(context: Context): String {
        val identifier = context.resources.getIdentifier(
            "vizbee_app_id",
            "string",
            context.packageName
        )
        if (identifier == 0) {
            return ""
        }
        return context.getString(identifier)
    }

    /**
     * Set video player adapter with app's media
     * object. This method will convert the
     * app's media object to Vizbee VideoInfo
     * and then call setPlayerAdapter.
     *
     *
     * Invoke this method just after a new video
     * is loaded in your video player.
     *
     * @param mediaItem App's internal media object
     */
    fun setPlayerAdapter(
        mediaItem: Video?,
        mediaSessionCompat: MediaSessionCompat,
        playerListener: MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener
    ) {
        mediaItem?.let {
            playerAdapterHandler.setPlayerAdapter(it, mediaSessionCompat, playerListener)
        }
    }

    /**
     * Reset video player adapter.
     *
     *
     * Invoke this method just after a video
     * ends or is interrupted in your video player.
     */
    fun resetPlayerAdapter() {
        playerAdapterHandler.resetPlayerAdapter()
    }

    fun clearAppReady() {
        appLifecycleAdapter.clearAppReady()
    }

    fun setAppReady(appReadyModel: AppReadyModel) {
        appLifecycleAdapter.setAppReady(appReadyModel)
    }
}