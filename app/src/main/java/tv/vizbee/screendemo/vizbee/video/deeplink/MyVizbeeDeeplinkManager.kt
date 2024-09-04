package tv.vizbee.screendemo.vizbee.video.deeplink

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import tv.vizbee.screendemo.vizbee.applifecycle.AppReadyModel
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screen.api.adapter.VizbeePlayerAdapter
import tv.vizbee.screen.api.messages.PlaybackStatus
import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screen.api.messages.VideoStatus
import tv.vizbee.screendemo.MainActivity

/**
 * #VizbeeGuide Implement deeplinkVideo() method as described.
 *
 * This class handles deep links and their failures.
 *
 * @property appReadyModel app ready model to get access to integration adapter
 */
class MyVizbeeDeeplinkManager(private val appReadyModel: AppReadyModel) : VizbeeDeeplinkManager {

    private var videoInfo: VideoInfo? = null

    /**
     * Fulfils the deeplink request.
     *
     * #VizbeeGuide Implement this method to read video information from videoInfo and deeplink to the right video.
     *
     * @param videoInfo The information about the video or audio to start.
     * @param positionMs The start position of the video or audio in milliseconds.
     */
    override fun deeplinkVideo(videoInfo: VideoInfo, positionMs: Long) {
        this.videoInfo = videoInfo

        val context = appReadyModel.activity

        // 1. Read the required information from videoInfo.
        val customMetadata = videoInfo.customMetadata
        val streamType = customMetadata.optString("streamType") ?: "vod"

        // 2. Deeplink the video.
        Log.d(
            LOG_TAG,
            "Deeplink is invoked with context = $context\nvideoInfo = $videoInfo\nposition = $positionMs"
        )
        val intent = Intent(appReadyModel.activity, MainActivity::class.java).apply {
            putExtra("guid", videoInfo.guid)
            putExtra("title", videoInfo.title)
            putExtra("isLive", videoInfo.isLive)
            putExtra("videoUrl", videoInfo.videoURL)
            putExtra("imageUrl", videoInfo.imageURL)
            putExtra("streamType", streamType)
            putExtra("position", positionMs)
        }

        context.startActivity(intent)
    }

    /**
     * Handles a deep link failure by sending an INTERRUPTED PlaybackStatus so that the mobile player card is dismissed.
     *
     * #VizbeeGuide Do not modify this method.
     */
    override fun handleDeeplinkFailure() {

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
    override fun sendFakeDeeplinkFailure(videoInfo: VideoInfo) {
        this.videoInfo = videoInfo
        handleDeeplinkFailure()
    }

    companion object {
        const val LOG_TAG = "MyVizbeeDeeplinkManager"
    }
}