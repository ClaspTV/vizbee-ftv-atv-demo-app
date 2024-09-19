package com.example.app.vizbee.video.deeplink

import com.example.app.vizbee.applifecycle.AppReadyModel
import com.example.app.vizbee.applifecycle.VizbeeAppLifecycleAdapter
import tv.vizbee.screen.api.adapter.VizbeeAppAdapter
import tv.vizbee.screen.api.messages.VideoInfo

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This class implements VizbeeAppAdapter to handle all "start or deeplink to a new video" command sent by your mobile app.
 * @property appLifecycleAdapter Vizbee lifecycle adapter implementation
 */
class MyVizbeeAppAdapter(
    private val appLifecycleAdapter: VizbeeAppLifecycleAdapter
) : VizbeeAppAdapter() {

    private var startVideoRequest: StartVideoRequest? = null

    init {
        // Add onAppReady listener to the lifecycleAdapter
        appLifecycleAdapter.addAppLifecycleListener(object : VizbeeAppLifecycleAdapter.AppLifecycleListener {

            override fun onAppReady(appReadyModel: AppReadyModel) {
                startVideoRequest?.let {
                    appReadyModel.deeplinkManager.deeplinkVideo(it.videoInfo, it.positionMs)
                    startVideoRequest = null
                }
            }

            override fun onAppUnReady() {
                // empty
            }

        })
    }

    /**
     * Handle a request to start a video or audio deep-link from sender.
     * This method should setup app state to correctly start playback of new video or audio.
     * @param videoInfo The information about the video or audio to start.
     * @param positionMs The start position of the video or audio in milliseconds.
     */
    override fun onStart(videoInfo: VideoInfo, positionMs: Long) {
        super.onStart(videoInfo, positionMs)

        if (appLifecycleAdapter.isAppReady()) {
            val appReadyModel = appLifecycleAdapter.getAppReadyModel()
            appReadyModel?.deeplinkManager?.deeplinkVideo(videoInfo, positionMs)
        } else {
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
}