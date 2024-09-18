package tv.vizbee.screendemo.vizbee.video.deeplink

import android.util.Log
import tv.vizbee.screen.api.adapter.VizbeeAppAdapter
import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screendemo.vizbee.applifecycle.AppReadyModel
import tv.vizbee.screendemo.vizbee.applifecycle.VizbeeAppLifecycleAdapter
import tv.vizbee.utils.Logger

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This class implements VizbeeAppAdapter to handle all "start or deeplink to a new video" command sent by your mobile app.
 *
 * @property appLifecycleAdapter Vizbee lifecycle adapter implementation
 */
class MyVizbeeAppAdapter(private val appLifecycleAdapter: VizbeeAppLifecycleAdapter) : VizbeeAppAdapter() {

    private var startVideoRequest: StartVideoRequest? = null

    init {
        appLifecycleAdapter.addAppLifecycleListener(object : VizbeeAppLifecycleAdapter.AppLifecycleListener {
            override fun onAppReady(appReadyModel: AppReadyModel) {
                Log.v(LOG_TAG, " do we have a saved start video request? = ${startVideoRequest != null}")

                startVideoRequest?.let {
                    appReadyModel.deeplinkManager.deeplinkVideo(it.videoInfo, it.positionMs)
                    startVideoRequest = null
                } ?: kotlin.run { }
            }

            override fun onAppUnReady() {
                // If you need to uninitialized any resources, do it here.
            }
        })
    }

    // region Handling Start Video

    /**
     * Handle a request to start a video or audio deep-link from sender.
     * This method should setup app state to correctly start playback of new video or audio.
     * @param videoInfo The information about the video or audio to start.
     * @param positionMs The start position of the video or audio in milliseconds.
     */
    override fun onStart(videoInfo: VideoInfo, positionMs: Long) {
        super.onStart(videoInfo, positionMs)

        //--------
        // handle deeplink sent from mobile apps
        //--------

        // Deep linking can use default VideoInfo fields or custom metadata
        Log.v(LOG_TAG, "onStart: videoInfo = $videoInfo \n position = $positionMs")

        if (appLifecycleAdapter.isAppReady()) {

            Logger.i(LOG_TAG, "App is ready. Proceeding for deeplink next steps")
            val appReadyModel = appLifecycleAdapter.getAppReadyModel()
            appReadyModel?.deeplinkManager?.deeplinkVideo(videoInfo, positionMs)
        } else {

            Logger.v(LOG_TAG, "App is not ready yet. Saving start video.")
            startVideoRequest = StartVideoRequest(videoInfo, positionMs)
        }
    }

    /**
     * This data class captures the video request sent by mobile via the Vizbee SDK.
     *
     * @param videoInfo video information in Vizbee format
     * @property positionMs The start position of the video or audio in milliseconds.
     */
    data class StartVideoRequest(val videoInfo: VideoInfo, val positionMs: Long)

    // endregion

    companion object {
        private const val LOG_TAG = "VZB_MyVizbeeAppAdapter"
    }
}