package com.example.app.vizbee.video.playback

import android.annotation.SuppressLint
import android.support.v4.media.session.MediaSessionCompat
import tv.vizbee.screen.api.adapter.MediaSessionCompatPlayerAdapter
import tv.vizbee.screen.api.messages.AdStatus
import tv.vizbee.screen.api.messages.PlaybackStatus
import tv.vizbee.screen.api.messages.VideoStatus
import tv.vizbee.screen.api.messages.VideoTrackInfo

/**
 * #VizbeeGuide Do not modify this file.
 * Note: If your player doesn't expose ad duration and position, set them to -1 for a graceful handling.
 *
 * This adapter class is a bridge between the app's player and the Vizbee SDK. It handles the player
 * commands sent by mobile via the SDK by passing them to the player. It also gets video status from
 * the player and passes it over to the Vizbee SDK so that the mobile player card is updated with
 * the latest status of the video. Note that this class overrides a built-in template class
 * MediaSessionCompatPlayerAdapter that takes care of handling most of the player commands by
 * passing them to the media session supplied to it. This class has to handle only closed captions
 * toggle command.
 *
 * @property mediaSessionCompat app's media session object
 * @property playerListener an implementation of the PlayerListener interface for this object to query
 * about player status and pass commands to the player.
 *
 * Note:
 * If your app has a standard MediaSession/MediaSessionCompat tied to your video player, use this adapter to create
 * the Vizbee player adapter. If your app has a non-standard interface to your video player, we recommned using
 * the MyVizbeePlayerAdapter to create a customized Vizbee player adapter.
 */
@SuppressLint("LongLogTag")
class MyVizbeeMediaSessionCompatPlayerAdapter(
    mediaSessionCompat: MediaSessionCompat,
    private val playerListener: PlayerListener
) : MediaSessionCompatPlayerAdapter(mediaSessionCompat) {

    /**
     * This interface declares the methods this adapter requires to interact with the player.
     * Make your fragment/activity implement this interface and interact with the player.
     */
    interface PlayerListener {
        fun isContentPlaying(): Boolean
        fun getContentPosition(): Long
        fun getDuration(): Long
        fun isAdPlaying(): Boolean
        fun getAdPosition(): Long
        fun getAdDuration(): Long
        fun toggleClosedCaptions()
        fun isClosedCaptioning(): Boolean
    }

    private var isPlayingContent = true

    /**
     * This method delivers the closed captions command from the mobile. The app just needs to
     * toggle the closed captions on the player.
     *
     * @param activeTrackInfo closed captions track info. this is mostly unused assuming the player
     * supports one active track info at a time that can be toggled.
     */
    override fun onSelectActiveTrackInfo(activeTrackInfo: VideoTrackInfo?) {
        super.onSelectActiveTrackInfo(activeTrackInfo)
        playerListener.toggleClosedCaptions()
    }

    /**
     * This method returns the video status obtained from the player. Implementing this method makes
     * sure that the mobile player card is updated with the latest playback status of the video.
     *
     * @return the current playback status of the video.
     */
    override fun getVideoStatus(): VideoStatus {
        val videoStatus = VideoStatus()
        videoStatus.mPlaybackStatus = PlaybackStatus.UNKNOWN
        videoStatus.durationMs = -1
        if (playerListener.isAdPlaying()) {

            // ads start
            if (isPlayingContent) {
                adStatusListener?.onAdStart(UNKNOWN)
                isPlayingContent = false
            }
            videoStatus.mPlaybackStatus = PlaybackStatus.PAUSED_BY_AD
            videoStatus.durationMs = -1
            videoStatus.positionMs = -1
        } else {

            // ads end
            if (!isPlayingContent) {
                adStatusListener?.onAdCompleted()
                isPlayingContent = true
            }

            when {
                playerListener.isContentPlaying() -> {
                    videoStatus.mPlaybackStatus = PlaybackStatus.PLAYING
                }

                else -> {
                    videoStatus.mPlaybackStatus = PlaybackStatus.PAUSED_BY_USER
                }
            }

            val contentPosition = playerListener.getContentPosition()
            videoStatus.positionMs = contentPosition
            videoStatus.durationMs = playerListener.getDuration()
        }

        return videoStatus
    }

    /**
     * This method returns the currently playing ad status. This method will be invoked by the SDK
     * during an AD.
     * If you don't have access to the AD position & duration, set them to -1.
     * If you set valid AD position & duration, the right AD counter will be shown on the mobile player card.
     * If you set -1 for AD position & duration, the mobile player card will just indicate that an AD is playing.
     * @return the AD's current status including its position, duration and its playback status.
     */
    override fun getAdStatus(): AdStatus? {
        if (playerListener.isAdPlaying()) {
            val adStatus = AdStatus()
            adStatus.mPlaybackStatus = PlaybackStatus.PLAYING
            adStatus.positionMs = playerListener.getAdPosition()
            adStatus.durationMs = playerListener.getAdDuration()
            return adStatus
        }
        adStatusListener?.onAdCompleted()
        return super.getAdStatus()
    }

    /**
     * This method is invoked by the SDK to get the current closed captions information.
     *
     * @return null if the closed captions is OFF and a valid track info if the closed captions is ON.
     */
    override fun getActiveTrackInfo(): VideoTrackInfo? {
        return if (!playerListener.isClosedCaptioning()) {
            null
        } else {
            VideoTrackInfo.Builder(1, VideoTrackInfo.TYPE_TEXT)
                .setLanguage("en")
                .setContentId("contentId")
                .setContentType("contentType")
                .build()
        }
    }

    companion object {
        private const val UNKNOWN: String = "unknown"
    }
}
