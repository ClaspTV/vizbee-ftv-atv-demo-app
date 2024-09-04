package tv.vizbee.screendemo.vizbee.video.deeplink

import tv.vizbee.screen.api.messages.VideoInfo

/**
 * #VizbeeGuide Do not modify this file.
 *
 * This interfaces captures the methods needed to deep link a video and handle its failure.
 * Implement this interface to deep link the given video and handle its failure by sending a status
 * to the Vizbee SDK.
 */
interface VizbeeDeeplinkManager {

    /**
     * Fulfils the deeplink request.
     *
     * @param videoInfo video info in Vizbee format. Unwrap this to find information required for deep linking a video.
     * @param positionMs position in milliseconds.
     */
    fun deeplinkVideo(videoInfo: VideoInfo, positionMs: Long)

    /**
     * Handles a deep link failure by sending an INTERRUPTED PlaybackStatus so that the mobile player card is dismissed.
     */
    fun handleDeeplinkFailure()

    /**
     * Simulates a deep link failure to dismiss the mobile player card when the deep link is waiting
     * for a sign in.
     *
     * @param videoInfo video info in Vizbee format.
     */
    fun sendFakeDeeplinkFailure(videoInfo: VideoInfo)
}