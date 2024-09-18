package com.example.app.vizbee.video.playback

import android.support.v4.media.session.MediaSessionCompat
import com.example.video.AppVideoInfo
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screen.api.messages.VideoTrackInfo

/**
 * #VizbeeGuide Implement setPlayerAdapter() and getVideoInfo() as described.
 *
 * This class is a helper class for the app to interact with the Vizbee API class in setting and
 * resetting the player adapter.
 *
 * Pass the required video information in app's format (AppVideoInfo) and convert it to
 * Vizbee's video info using getVideoInfo() method.
 */
class MyVizbeePlayerAdapterHandler(private val isVizbeeEnabled: Boolean) {

    private var playerAdapter: MyVizbeeMediaSessionCompatPlayerAdapter? = null

    /**
     * #VizbeeGuide For the first param, pass the video information in your app's format.
     *
     * Invoke this method just after a new video is loaded in your video player.
     *
     * @param appVideoInfo App's internal media object
     * @param mediaSessionCompat MediaControllerCompat object
     */
    fun setPlayerAdapter(
        appVideoInfo: AppVideoInfo?,
        mediaSessionCompat: MediaSessionCompat?,
        vizbeePlayerListener: MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener
    ) {

        if (!isVizbeeEnabled) {
            return
        }

        // Reset before setting player adapter to clean up previous transactions
        resetPlayerAdapter()

        mediaSessionCompat?.let {

            val videoInfo = getVideoInfo(appVideoInfo)
            playerAdapter = MyVizbeeMediaSessionCompatPlayerAdapter(
                mediaSessionCompat,
                vizbeePlayerListener
            )
            playerAdapter?.let { Vizbee.getInstance().setPlayerAdapter(videoInfo, it) }
        }
    }

    /**
     * Reset video player adapter.
     *
     * Invoke this method just after a video
     * ends or is interrupted in your video player.
     */
    fun resetPlayerAdapter() {

        if (!isVizbeeEnabled) {
            return
        }

        playerAdapter = null
        Vizbee.getInstance().resetPlayerAdapter()
    }

    /**
     * This method converts the app's media object
     * to Vizbee VideoInfo.
     *
     * @param mediaItem App's internal data structure for media.
     * @return videoInfo Returns the media represented as VideoInfo.
     */
    private fun getVideoInfo(mediaItem: MyMediaItem?): VideoInfo? {
        if (null == mediaItem) {
            return null
        }
        val videoInfo = VideoInfo()
        videoInfo.guid = getGUID(mediaItem)
        videoInfo.isLive = mediaItem.isLiveItem()
        videoInfo.title = mediaItem.getItemTitle()
        videoInfo.description = mediaItem.getItemDescription()
        videoInfo.imageURL = mediaItem.getThumbnailImageUrl()
        // To enable closed captions
        val tracksList = ArrayList<VideoTrackInfo>()
        val videoTrackInfo = VideoTrackInfo.Builder(1, VideoTrackInfo.TYPE_TEXT)
            .setLanguage("en")
            .setContentId("contentId")
            .setContentType("contentType")
            .build()
        tracksList.add(videoTrackInfo)
        videoInfo.tracks = tracksList
        return videoInfo
    }
}