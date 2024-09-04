package tv.vizbee.screendemo.vizbee.applifecycle

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This interface is to let the Vizbee integration know app's readiness to be able to handle its
 * deep links, sign in requests etc. The implementation of this class should provide a way to inform
 * the app's readiness and the required AppReadyModel that should contain all the objects necessary
 * to handle the deep link requests and sign in requests coming from mobile.
 */
interface VizbeeAppLifecycleAdapter {

    /**
     * This interface is for the Vizbee integration to register for app's readiness and unreadiness
     */
    interface AppLifecycleListener {

        /**
         * Called when the app is ready, typically when the activity is created and all the models
         * are initialised for the Vizbee integration to consume. Vizbee integration would wait for
         * this and process any pending start-video or sign-in requests.
         */
        fun onAppReady(appReadyModel: AppReadyModel)

        /**
         * Called when the app becomes unready. Vizbee integration would be aware of this and perform
         * any clean up.
         */
        fun onAppUnReady()
    }

    /**
     * Registers a lifecycle listener
     */
    fun addAppLifecycleListener(appLifecycleListener: AppLifecycleListener)

    /**
     * Returns true if the app is ready for Vizbee integration to be able to process start-video or
     * sign-in requests
     */
    fun isAppReady(): Boolean

    /**
     * App should call this method when its models are ready for the Vizbee integration to process
     */
    fun setAppReady(appReadyModel: AppReadyModel)

    /**
     * App should call this method as soon as its models' scope is over. Typically, when the activity is destroyed.
     */
    fun clearAppReady()

    /**
     * Returns the app ready model
     */
    fun getAppReadyModel(): AppReadyModel?

    /**
     * App should call this method when it starts/stops playing a video
     */
    fun setVideoPlaying(isVideoPlaying: Boolean)

    /**
     * Returns if the app is playing any video or not
     */
    fun isVideoPlaying(): Boolean
}