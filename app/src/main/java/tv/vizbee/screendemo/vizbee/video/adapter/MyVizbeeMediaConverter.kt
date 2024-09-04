package tv.vizbee.screendemo.vizbee.video.adapter

import tv.vizbee.screen.api.messages.VideoInfo
import tv.vizbee.screen.api.messages.VideoTrackInfo
import tv.vizbee.screendemo.model.Video


/**
 * #VizbeeGuide Implement getVideoInfo() as described.
 *
 * Helper to convert app's internal "MediaItem" to Vizbee VideoInfo
 */
class MyVizbeeMediaConverter {

    /**
     * This method converts the app's media object
     * to Vizbee VideoInfo.
     *
     * @param mediaItem App's internal data structure for media.
     * @return videoInfo Returns the media represented as VideoInfo.
     */
    fun getVideoInfo(mediaItem: Video?): VideoInfo? {
        if (null == mediaItem) {
            return null
        }

        val videoInfo = VideoInfo()
        videoInfo.guid = mediaItem.guid
        videoInfo.isLive = mediaItem.isLive
        videoInfo.title = mediaItem.title
//        videoInfo.description = mediaItem.describeContents()
        videoInfo.imageURL = mediaItem.imageUrl
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
