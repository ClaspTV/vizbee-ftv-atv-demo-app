package tv.vizbee.screendemo.vizbee.video.adapter

import android.support.v4.media.session.MediaSessionCompat
import tv.vizbee.screen.api.Vizbee
import tv.vizbee.screendemo.model.Video

/**
 * #VizbeeGuide Implement setPlayerAdapter() as described.
 *
 * This class is a helper class for the app to interact with the Vizbee API class in setting and
 * resetting the player adapter.
 *
 * Pass the required video information in app's format (AppVideoInfo) and convert it to
 * Vizbee's video info using AppVideoConverter instance.
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
        video: Video?,
        mediaSessionCompat: MediaSessionCompat?,
        vizbeePlayerListener: MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener
    ) {

        if (!isVizbeeEnabled) {
            return
        }

        // Reset before setting player adapter to clean up previous transactions
        resetPlayerAdapter()

        mediaSessionCompat?.let {

            val videoInfo = MyVizbeeMediaConverter().getVideoInfo(video)
            videoInfo?.let {
                playerAdapter = MyVizbeeMediaSessionCompatPlayerAdapter(mediaSessionCompat, vizbeePlayerListener)
                playerAdapter?.let { Vizbee.getInstance().setPlayerAdapter(videoInfo, it) }
            }
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
}