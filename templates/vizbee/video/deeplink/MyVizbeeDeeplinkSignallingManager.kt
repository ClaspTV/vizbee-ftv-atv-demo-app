package com.example.app.vizbee.video.deeplink

import android.os.Handler
import android.os.Looper
import com.example.app.vizbee.applifecycle.AppReadyModel
import com.example.app.vizbee.applifecycle.VizbeeAppLifecycleAdapter
import com.example.app.vizbee.homesso.MyVizbeeHomeSSOAdapter
import com.example.app.vizbee.homesso.VizbeeSignInStatusListener
import tv.vizbee.screen.api.messages.VideoInfo

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This class signals when to deep link a video. It waits for sign in and signals deep link at the
 * right time.
 * Details:
 * - If the sign in is in progress, it will wait for its success/failure and then deep links.
 * - If the app is not ready, HomeSSO will not be ready. So, this will wait HomeSSO's readiness and
 *   then checks the sign in status.
 * - We optimise the process by skipping sign in checks for the non first videos, if the first video
 *   finding is supported by the platform.
 *
 * @property appLifecycleAdapter an implementation of VizbeeAppLifecycleAdapter
 * @property firstVideoUseCaseInfoProvider an implementation of FirstVideoUseCaseInfoProvider for this object to query about first video
 * @property homeSSOAdapter an implementation of VizbeeHomeSSOAdapter to query sign in status
 */
class MyVizbeeDeeplinkSignallingManager(
    private val appLifecycleAdapter: VizbeeAppLifecycleAdapter,
    private val firstVideoUseCaseInfoProvider: FirstVideoUseCaseInfoProvider,
    private val homeSSOAdapter: MyVizbeeHomeSSOAdapter
) {
    private var startVideoRequest: StartVideoRequest? = null
    private var deeplinkCallback: ((appReadyModel : AppReadyModel) -> Unit)? = null
    private var waitingForSignInCallback: ((appReadyModel : AppReadyModel) -> Unit)? = null

    // region Constructor

    init {
        homeSSOAdapter.homeSSOReadyListener = object : MyVizbeeHomeSSOAdapter.HomeSSOReadyListener {
            override fun onHomeSSOReady() {
                startVideoRequest?.let {
                    performSignInChecksAndDeeplink(it.videoInfo, it.positionMs)
                    startVideoRequest = null;
                }
                homeSSOAdapter.homeSSOReadyListener = null
            }
        }
    }

    // endregion

    // region API: Signal deeplink

    /**
     *
     */
    fun signalDeeplink(
        deeplinkCallback: ((appReadyModel : AppReadyModel) -> Unit),
        waitingForSignInCallback: ((appReadyModel : AppReadyModel) -> Unit),
        videoInfo: VideoInfo,
        positionMs: Long
    ) {
        this.deeplinkCallback = deeplinkCallback
        this.waitingForSignInCallback = waitingForSignInCallback
        performSignInChecksAndDeeplink(videoInfo, positionMs)
    }

    // endregion

    // region Sign in Checks

    private fun performSignInChecksAndDeeplink(
        videoInfo: VideoInfo,
        positionMs: Long
    ) {
        if (!homeSSOAdapter.isHomeSSOReady) {
            startVideoRequest = StartVideoRequest(videoInfo, positionMs)
            return
        }

        if (homeSSOAdapter.isSignedIn()) {
            deeplink()
        } else {
            performFirstVideoBasedSignInChecksAndDeeplink()
        }
    }

    private fun performFirstVideoBasedSignInChecksAndDeeplink() {

        // Checking if the platform (FTV/ATV) allows to use the isFirstVideo logic
        // NOTE: In midstream case, for the first video after a sender is connected, there is a
        // chance that sign in request comes later than start video. To handle that, we'll make
        // start video wait for 1 sec. For subsequent start video requests, sign in would come
        // before that. In these scenarios, we should make video wait only if the sign in is in
        // progress otherwise we'll deeplink. For progress cases, we'll wait for sign in
        // success/failure

        if (firstVideoUseCaseInfoProvider.shouldCheckForFirstVideo()) {
            // Checking if the start video request received for first time in app sender connected session
            if (firstVideoUseCaseInfoProvider.isFirstVideoRequest()) {
                Looper.myLooper()?.let {
                    Handler(it).postDelayed({
                        doSignInProgressCheckAndDeeplink()
                    }, 1000)
                }
            } else {
                deeplink()
            }
        } else {
            doSignInProgressCheckAndDeeplink()
        }
    }

    private fun doSignInProgressCheckAndDeeplink() {
        // If the sign in is in progress, let's wait for it. Otherwise, just deep link.
        if (homeSSOAdapter.isSignInInProgress) {
            waitForSignInUpdateAndDeeplink()
        } else {
            deeplink()
        }
    }

    private fun waitForSignInUpdateAndDeeplink() {
        appLifecycleAdapter.getAppReadyModel()?.let { waitingForSignInCallback?.invoke(it) }

        homeSSOAdapter.signInStatusListener = object : VizbeeSignInStatusListener {
            override fun onProgress(signInType: String, regCode: String?) {
                // do nothing
            }

            override fun onSuccess(signInType: String) {
                deeplink()
            }
            override fun onFailure(signInType: String, isCancelled: Boolean) {
                deeplink()
            }
        }
    }

    // Inform the adapter to deeplink the video
    private fun deeplink() {
        appLifecycleAdapter.getAppReadyModel()?.let { this.deeplinkCallback?.invoke(it) }
        homeSSOAdapter.signInStatusListener = null
    }

    interface FirstVideoUseCaseInfoProvider {
        fun shouldCheckForFirstVideo(): Boolean
        fun isFirstVideoRequest(): Boolean
    }

    // endregion

    // region Models

    /**
     * This data class captures the video request sent by mobile via the Vizbee SDK.
     *
     * @param videoInfo video information in Vizbee format
     * @property positionMs The start position of the video or audio in milliseconds.
     */
    data class StartVideoRequest(val videoInfo: VideoInfo, val positionMs: Long)

    // endregion
}