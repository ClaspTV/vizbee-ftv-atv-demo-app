package tv.vizbee.screendemo.vizbee.video.deeplink

import android.app.Application
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
 * @property application application object
 * @property appLifecycleAdapter Vizbee lifecycle adapter implementation
 */
class MyVizbeeAppAdapter(
    private val application: Application,
    private val appLifecycleAdapter: VizbeeAppLifecycleAdapter
) : VizbeeAppAdapter() {

    private var startVideoRequest: StartVideoRequest? = null

    init {
        appLifecycleAdapter.addAppLifecycleListener(object : VizbeeAppLifecycleAdapter.AppLifecycleListener {
            override fun onAppReady(appReadyModel: AppReadyModel) {
                val haveSavedStartVideoRequest = (null != startVideoRequest)
                Log.v(LOG_TAG, " do we have a saved start video request? = $haveSavedStartVideoRequest")

                if (haveSavedStartVideoRequest) {
                    startVideoRequest?.let {
                        onStart(it.videoInfo, it.positionMs)
                        startVideoRequest = null;
                    }
                }
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
        if (!appLifecycleAdapter.isAppReady()) {
            Logger.v(LOG_TAG, "App is not ready yet. Saving start video.")
            startVideoRequest = StartVideoRequest(videoInfo, positionMs)
            return
        }

        Logger.i(LOG_TAG, "App is ready. Proceeding for deeplink next steps")
        val appReadyModel = appLifecycleAdapter.getAppReadyModel()
        appReadyModel?.let {

            // Note: if app needs to wait for the sign in by a specific sign in type,
            // please replace the following line by authStatusProvider.isSignedIn(signIntypeRequired)

            // Check if user is already signed in, if yes proceed for deeplink
            val json = videoInfo.customMetadata
            Log.v(LOG_TAG, "onStart: custom metadata $json")
            it.deeplinkManager.deeplinkVideo(videoInfo, positionMs)
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