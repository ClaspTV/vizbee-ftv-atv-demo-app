package om.myapp

import tv.vizbee.screen.api.adapter.VizbeePlayerAdapter
import tv.vizbee.screen.api.messages.AdStatus
import tv.vizbee.screen.api.messages.PlaybackStatus
import tv.vizbee.screen.api.messages.VideoStatus
import android.util.Log

/**
 * NOTE:
 * IF YOUR APP HAS A NON-STANDARD INTERFACE TO YOUR VIDEO PLAYER, USE THIS ADAPTER TO CREATE A CUSTOMIZED VIZBEE PLAYER ADAPTER.
 * REMOVE THE MYVIZBEEMEDIASESSIONCOMPATPLAYERADAPTER FROM THE COPIED TEMPLATES.
 *
 * This adapter class is a bridge between the app's player and the Vizbee SDK. It handles the player
 * commands sent by mobile via the SDK by passing them to the player. It also gets video status from
 * the player and passes it over to the Vizbee SDK so that the mobile player card is updated with
 * the latest status of the video.
 *
 * @property contentPlayer An instance of the current video player handler/manager.
 */
class MyVizbeePlayerAdapter : VizbeePlayerAdapter() {

    companion object {
        private const val LOG_TAG = "MyVizbeePlayerAdapter"
    }

    private var myVideoPlayer: MyVideoPlayer? = null

    fun setVideoPlayer(videoPlayer: MyVideoPlayer) {
        myVideoPlayer = videoPlayer
    }

    override fun play() {
        Log.v(LOG_TAG, "PLAY command from mobile")
        myVideoPlayer?.play()
    }

    override fun pause() {
        Log.v(LOG_TAG, "PAUSE command from mobile")
        myVideoPlayer?.pause()
    }

    override fun seek(position: Int) {
        Log.v(LOG_TAG, "SEEK command from mobile")
        myVideoPlayer?.seek(position)
    }

    override fun stop(reason: Int) {
        Log.v(LOG_TAG, "STOP command from mobile")
        myVideoPlayer?.stop()
    }

    /**
     * This method delivers the closed captions command from the mobile. The app just needs to
     * toggle the closed captions on the player.
     *
     * @param activeTrackInfo closed captions track info. this is mostly unused assuming the player
     * supports one active track info at a time that can be toggled.
     */
    override fun onSelectActiveTrackInfo(activeTrackInfo: VideoTrackInfo) {
        super.onSelectActiveTrackInfo(activeTrackInfo)
        Log.i(LOG_TAG, "onSelectActiveTrackInfo $activeTrackInfo")
        // turn ON/OFF closed captions
    }

    /**
     * This method is invoked by the SDK to get the current closed captions information.
     *
     * @return null if the closed captions is OFF and a valid track info if the closed captions is ON.
     */
    override fun getActiveTrackInfo(): VideoTrackInfo? {
        Log.v(LOG_TAG, "getActiveTrackInfo isClosedCaptioning = $isClosedCaptioning")
        return if (isClosedCaptionsOFF) {
            null
        } else {
            // Convert app's track info to VideoTrackInfo and return
            /*
            return VideoTrackInfo.Builder(1, VideoTrackInfo.TYPE_TEXT)
                .setLanguage("en")
                .setLanguage("en")
                .setContentId("contentId")
                .setContentType("contentType")
                .build()
            */
        }
    }

    private var isPlayingContent = true

    /**
     * This method returns the video status obtained from the player. Implementing this method makes
     * sure that the mobile player card is updated with the latest playback status of the video.
     *
     * @return the current playback status of the video.
     */
    override fun getVideoStatus(): VideoStatus? {
        Log.v(LOG_TAG, "Sending VIDEO_STATUS to mobile")

        val videoStatus = VideoStatus()
        videoStatus.mPlaybackStatus = PlaybackStatus.UNKNOWN
        videoStatus.mDuration = -1

        myVideoPlayer?.let {
            if (it.isPlayingAd()) {
                // ads start
                if (isPlayingContent) {
                    getAdStatusListener()?.onAdStart("unknown")
                    isPlayingContent = false
                }

                videoStatus.mPlaybackStatus = PlaybackStatus.PAUSED_BY_AD
                videoStatus.mDuration = -1
                videoStatus.mPosition = -1
            } else {
                // ads end
                if (!isPlayingContent) {
                    getAdStatusListener()?.onAdCompleted()
                    isPlayingContent = true
                }

                // set playback status
                val playbackState = it.playbackState
                playbackState?.let { state ->
                    videoStatus.mPlaybackStatus = when (state.state) {
                        PlaybackStateCompat.STATE_PLAYING -> PlaybackStatus.PLAYING
                        PlaybackStateCompat.STATE_PAUSED -> PlaybackStatus.PAUSED_BY_USER
                        PlaybackStateCompat.STATE_BUFFERING -> PlaybackStatus.BUFFERING
                        PlaybackStateCompat.STATE_ERROR -> PlaybackStatus.FAILED
                        PlaybackStateCompat.STATE_STOPPED -> PlaybackStatus.INTERRUPTED
                        PlaybackStateCompat.STATE_NONE -> PlaybackStatus.UNKNOWN
                        else -> PlaybackStatus.UNKNOWN
                    }
                }

                videoStatus.mPosition = it.currentPosition.toInt()
                videoStatus.mDuration = it.duration.toInt()
            }
        }

        return videoStatus
    }

    /**
     * This method returns the currently playing ad status. This method will be invoked by the SDK
     * during an AD.
     * If you don't have access to the AD position & duration, set them to -1.
     * If you set valid AD position & duration, the right AD counter will be shown on the mobile player card.
     * If you set -1 for AD position & duration, the mobile player card will just indicate that an AD is playing.
     *
     * @return the AD's current status including its position, duration and its playback status.
     */
    override fun getAdStatus(): AdStatus? {
        Log.v(LOG_TAG, "Sending AD_STATUS to mobile")

        return myVideoPlayer?.takeIf { it.isPlayingAd() }?.let {
            val adStatus = AdStatus()
            adStatus.mPlaybackStatus = PlaybackStatus.PLAYING
            adStatus.mPosition = it.currentPosition.toInt()
            adStatus.mDuration = it.duration.toInt()
            adStatus
        }
    }
}