package tv.vizbee.screendemo.utils

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.BaseMediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.util.Util
import tv.vizbee.screendemo.BuildConfig

object ExoplayerUtils {
    fun buildMediaSource(
        uri: Uri,
        mediaDataSourceFactory: DataSource.Factory,
        handler: Handler,
        overrideExtension: String,
        adaptiveListener: MediaSourceEventListener
    ): BaseMediaSource {
        val type =
            Util.inferContentType(
                (if (!TextUtils.isEmpty(overrideExtension))
                    ".$overrideExtension"
                else
                    uri.lastPathSegment)!!
            )
        return when (type) {
            C.TYPE_SS -> {
                val mediaSource =
                    SsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
                mediaSource.addEventListener(handler, adaptiveListener)
                return mediaSource
            }

            C.TYPE_DASH -> {
                val mediaSource =
                    DashMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
                mediaSource.addEventListener(handler, adaptiveListener)
                return mediaSource
            }

            C.TYPE_HLS -> {
                val mediaSource =
                    HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
                mediaSource.addEventListener(handler, adaptiveListener)
                return mediaSource
            }

            C.TYPE_OTHER -> {
                val mediaSource =
                    ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
                mediaSource.addEventListener(handler, adaptiveListener)
                return mediaSource
            }

            else -> {
                throw IllegalStateException("Unsupported type: $type")
            }
        }
    }

    /* Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    fun buildDataSourceFactory(context: Context, useBandwidthMeter: Boolean): DataSource.Factory {
        return buildDataSourceFactory(context)
    }

    fun buildDataSourceFactory(context: Context): DataSource.Factory {
        return DefaultDataSourceFactory(context, buildHttpDataSourceFactory(context))
    }

    fun buildHttpDataSourceFactory(context: Context): HttpDataSource.Factory {
        return DefaultHttpDataSource.Factory().setUserAgent(getUserAgent(context))
    }

    fun useExtensionRenderers(): Boolean {
        return false
//        return BuildConfig.FLAVOR.equals("withExtensions")
    }

    fun getUserAgent(context: Context?): String {
        return Util.getUserAgent(context!!, BuildConfig.APPLICATION_ID)
    }
}