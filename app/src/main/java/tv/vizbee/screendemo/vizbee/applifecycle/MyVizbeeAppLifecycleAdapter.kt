package tv.vizbee.screendemo.vizbee.applifecycle

import android.util.Log

/**
 * #VizbeeGuide Do not modify this file.
 * Call setAppReady(), clearAppReady() from Activity as described in the method documentation.
 * Call setVideoPlaying() with true/false when the video starts/stops to play.
 *
 * This class provides a way to inform the app's readiness and the required AppReadyModel that
 * should contain all the objects necessary to handle the deep link requests and sign in requests
 * coming from mobile.
 *
 * Create an instance of this class in the Application class and pass it to MyVizbeeAppAdapter
 */
class MyVizbeeAppLifecycleAdapter : VizbeeAppLifecycleAdapter {

    private var appReadyModel: AppReadyModel? = null
    private var videoPlaying: Boolean = false
    private val appLifecycleListeners: ArrayList<VizbeeAppLifecycleAdapter.AppLifecycleListener> = arrayListOf()

    /**
     * Registers a lifecycle listener.
     */
    override fun addAppLifecycleListener(appLifecycleListener: VizbeeAppLifecycleAdapter.AppLifecycleListener
    ) {
        this.appLifecycleListeners.add(appLifecycleListener)
    }

    /**
     * Returns true if the app is ready for Vizbee integration to be able to process start-video or
     * sign-in requests.
     */
    override fun isAppReady(): Boolean {
        return appReadyModel != null
    }

    /**
     * App should call this method when its models are ready for the Vizbee integration to process.
     */
    override fun setAppReady(appReadyModel: AppReadyModel) {

        // check if it's duplicate app ready before assigning the model
        val isDuplicate = isAppReady()
        this.appReadyModel = appReadyModel

        if (!isDuplicate) {
            appLifecycleListeners.forEach {
                it.onAppReady(appReadyModel)
            }
        } else {
            Log.i(LOG_TAG, "IGNORE_DUP_APP_READY")
        }
    }

    /**
     * App should call this method as soon as its models' scope is over. Typically, when the activity is destroyed.
     */
    override fun clearAppReady() {

        // reset app ready model
        this.appReadyModel = null
        appLifecycleListeners.forEach {
            it.onAppUnReady()
        }
    }

    /**
     * Returns the app ready model
     */
    override fun getAppReadyModel(): AppReadyModel? {
        return appReadyModel
    }

    /**
     * App should call this method when it starts/stops playing a video
     */
    override fun setVideoPlaying(isVideoPlaying: Boolean) {
        this.videoPlaying = isVideoPlaying
    }

    /**
     * Returns if the app is playing any video or not
     */
    override fun isVideoPlaying(): Boolean {
        return videoPlaying
    }

    companion object {
        const val LOG_TAG = "AppLifecycleAdapter"
    }
}