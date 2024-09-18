package com.example.app.vizbee.video.deeplink

import android.os.Handler
import android.os.Looper
import com.example.app.vizbee.applifecycle.AppReadyModel
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screen.api.adapter.VizbeePlayerAdapter
import tv.vizbee.screen.api.messages.PlaybackStatus
import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screen.api.messages.VideoStatus

/**
 * #VizbeeGuide Implement deeplinkVideo() method as described.
 *
 * This class handles deep links and their failures.
 *
 * @property appReadyModel app ready model to get access to integration adapter
 */
class MyVizbeeDeeplinkManager(private val appReadyModel: AppReadyModel) {

    private var videoInfo: VideoInfo? = null

    /**
     * Fulfils the deeplink request.
     *
     * #VizbeeGuide Implement this method to read video information from videoInfo and deeplink to the right video.
     *
     * @param videoInfo The information about the video or audio to start.
     * @param positionMs The start position of the video or audio in milliseconds.
     */
    fun deeplinkVideo(videoInfo: VideoInfo, positionMs: Long) {
        this.videoInfo = videoInfo

        // 1. Map Vizbee VideoInfo to your app's video model
        val appVideoModel = mapToAppVideoModel(videoInfo)

        // 2. Optional: Perform any necessary app state cleanup
        cleanupAppState()

        // 3. Invoke your app's deep linking mechanism
        try {
            yourAppDeepLinkHandler.startVideo(appVideoModel, positionMs)
        } catch (e: Exception) {
            // Handle any errors that occur during deep linking
            handleDeeplinkFailure()
        }
    }

    private fun mapToAppVideoModel(videoInfo: VideoInfo): YourAppVideoModel {
        // Implement mapping logic here
        // Example:
        val streamType = if (videoInfo.isLive) "live" else "vod"
        val guid = videoInfo.guid
        // Add more mapping as needed, eg., read custom metadata from video info using videoInfo.customMetadata
        return YourAppVideoModel(streamType, guid, /* other parameters */)
    }

    private fun cleanupAppState() {
        // Implement any necessary cleanup steps
        // For example:
        // appReadyModel.appViewModel.currentlyPlayingVideo?.stop()
        // appReadyModel.appViewModel.navigation.dismissAllPrompts()
        // appReadyModel.appViewModel.navigation.resetToHomeScreen()
    }

    /**
     * Handles a deep link failure by sending an INTERRUPTED PlaybackStatus so that the mobile player card is dismissed.
     *
     * #VizbeeGuide Do not modify this method.
     */
    fun handleDeeplinkFailure() {

        var sentLoadingStatus = false
        videoInfo?.let {
            Vizbee.getInstance().setPlayerAdapter(it, object : VizbeePlayerAdapter() {

                override fun play() {}

                override fun pause() {}

                override fun seek(p0: Long) {}

                override fun stop(p0: Int) {}

                override fun getVideoStatus(): VideoStatus {

                    val videoStatus = VideoStatus()
                    if (sentLoadingStatus) {
                        videoStatus.mPlaybackStatus = PlaybackStatus.INTERRUPTED
                        this@MyVizbeeDeeplinkManager.videoInfo = null
                    } else {
                        videoStatus.mPlaybackStatus = PlaybackStatus.LOADING
                        sentLoadingStatus = true
                    }
                    return videoStatus
                }
            })
        }
        Looper.myLooper()?.let {
            Handler(it).postDelayed({
                Vizbee.getInstance().resetPlayerAdapter()
            }, 1000L)
        }
    }

    /**
     * Simulates a deep link failure to dismiss the mobile player card when the deep link is waiting
     * for a sign in.
     *
     * #VizbeeGuide Do not modify this method.
     *
     * @param videoInfo video info in Vizbee format.
     */
    fun sendFakeDeeplinkFailure(videoInfo: VideoInfo) {
        this.videoInfo = videoInfo
        handleDeeplinkFailure()
    }
}