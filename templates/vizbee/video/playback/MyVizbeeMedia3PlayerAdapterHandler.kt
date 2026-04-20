package com.example.app.vizbee.video.playback

import androidx.media3.session.MediaSession
import com.example.video.AppVideoInfo
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screen.api.messages.VideoTrackInfo
import tv.vizbee.screen.api.adapter.Media3MediaSessionPlayerAdapter

/**
 * #VizbeeGuide Implement setPlayerAdapter() and getVideoInfo() as described.
 *
 * This class is a helper class for the app to interact with the Vizbee API class in setting and
 * resetting the player adapter.
 *
 * Pass the required video information in app's format (AppVideoInfo) and convert it to
 * Vizbee's video info using getVideoInfo() method.
 */
class MyVizbeeMedia3PlayerAdapterHandler(private val isVizbeeEnabled: Boolean) {

    private var playerAdapter: Media3MediaSessionPlayerAdapter? = null

    /**
     * #VizbeeGuide For the first param, pass the video information in your app's format.
     *
     * Invoke this method just after a new video is loaded in your video player.
     *
     * @param appVideoInfo App's internal media object
     * @param mediaSession MediaController object
     */
    fun setPlayerAdapter(
        appVideoInfo: AppVideoInfo?,
        mediaSession: MediaSession
    ) {

        if (!isVizbeeEnabled) {
            return
        }

        // Reset before setting player adapter to clean up previous transactions
        resetPlayerAdapter()

        mediaSession?.let {

            val videoInfo = getVideoInfo(appVideoInfo)
            playerAdapter = Media3MediaSessionPlayerAdapter(mediaSession)
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