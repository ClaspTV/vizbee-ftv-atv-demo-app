package tv.vizbee.screendemo.vizbee.video.deeplink

import android.content.Context
import android.os.Build
import tv.vizbee.screen.api.adapter.VizbeeAppAdapter
import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screendemo.vizbee.applifecycle.VizbeeAppLifecycleAdapter
import tv.vizbee.screendemo.vizbee.homesso.MyVizbeeHomeSSOAdapter

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This class implements VizbeeAppAdapter to handle all "start or deeplink to a new video" command sent by your mobile app.
 *
 * @property appLifecycleAdapter Vizbee lifecycle adapter implementation
 * @property homeSSOAdapter Vizbee HomeSSO adapter implementation
 */
class MyVizbeeAppAdapter(
    private val appLifecycleAdapter: VizbeeAppLifecycleAdapter,
    private val homeSSOAdapter: MyVizbeeHomeSSOAdapter
) :
    VizbeeAppAdapter(), MyVizbeeDeeplinkSignallingManager.FirstVideoUseCaseInfoProvider {

    // region Handling Start Video

    /**
     * Handle a request to start a video or audio deep-link from sender.
     * This method should setup app state to correctly start playback of new video or audio.
     * @param videoInfo The information about the video or audio to start.
     * @param positionMs The start position of the video or audio in milliseconds.
     */
    override fun onStart(videoInfo: VideoInfo, positionMs: Long) {
        super.onStart(videoInfo, positionMs)

        val deeplinkSignallingManager = MyVizbeeDeeplinkSignallingManager(
            appLifecycleAdapter,
            this,
            homeSSOAdapter
        )
        deeplinkSignallingManager.signalDeeplink(
            deeplinkCallback = { appReadyModel ->
                appReadyModel.deeplinkManager.deeplinkVideo(videoInfo, positionMs)
                isFirstVideoRequest = false
            },
            waitingForSignInCallback = { appReadyModel ->
                // send a temporary failure to dismiss the player card on the mobile
                if (!homeSSOAdapter.isMobileSignedIn) {
                    appReadyModel.deeplinkManager.sendFakeDeeplinkFailure(videoInfo)
                }
            },
            videoInfo = videoInfo,
            positionMs = positionMs
        )

    }

    // endregion

    // region Determining First Video
    var context:Context? = null
    fun setContext(context: Context) {
        this.context = context
    }
    /**
     * Returns true if deep linking has to check for whether it's a first video request after
     * connection or not. For non first video requests, we do not wait for sign in before deep
     * linking.
     * Currently, for FireTV, we do not check for first video.
     */
    override fun shouldCheckForFirstVideo(): Boolean {
        return !isFireTv(context)
    }

    /**
     * Returns true if the current build is running in FireTV.
     *
     * @param context Android Context object that's used to get package manager.
     */
    private fun isFireTv(context: Context?): Boolean {
        return Build.MODEL?.startsWith("AFT") == true ||
                context?.packageManager?.hasSystemFeature("amazon.hardware.fire_tv") == true
    }

    /**
     * Boolean and a getter for a first video check.
     */
    private var isFirstVideoRequest = true
    override fun isFirstVideoRequest(): Boolean {
        return isFirstVideoRequest
    }

    /**
     * Vizbee adapter method that informs when first sender is connected.
     */
    override fun onSendersActive() {
        super.onSendersActive()
        isFirstVideoRequest = true
    }

    /**
     * Vizbee adapter method that informs when last sender is disconnected.
     */
    override fun onSendersInactive() {
        super.onSendersInactive()
        isFirstVideoRequest = false
    }

    // endregion
}