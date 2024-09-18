package tv.vizbee.screendemo.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.ads.interactivemedia.v3.api.AdErrorEvent
import com.google.ads.interactivemedia.v3.api.AdErrorEvent.AdErrorListener
import com.google.ads.interactivemedia.v3.api.AdEvent
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventListener
import com.google.ads.interactivemedia.v3.api.AdEvent.AdEventType
import com.google.ads.interactivemedia.v3.api.AdsLoader
import com.google.ads.interactivemedia.v3.api.AdsLoader.AdsLoadedListener
import com.google.ads.interactivemedia.v3.api.AdsManager
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import tv.vizbee.screendemo.R
import tv.vizbee.screendemo.databinding.ActivityExoPlayerBinding
import tv.vizbee.screendemo.model.Video
import tv.vizbee.screendemo.utils.ExoplayerUtils
import tv.vizbee.screendemo.vizbee.VizbeeWrapper.Companion.isVizbeeEnabled
import tv.vizbee.screendemo.vizbee.VizbeeWrapper.Companion.vizbeeAppLifecycleAdapter
import tv.vizbee.screendemo.vizbee.video.playback.MyVizbeeMediaSessionCompatPlayerAdapter
import tv.vizbee.screendemo.vizbee.video.playback.MyVizbeePlayerAdapterHandler
import java.io.IOException

class ExoPlayerActivity : AppCompatActivity(), MediaSourceEventListener,
    AdErrorListener, AdEventListener, Player.Listener {
    private lateinit var binding: ActivityExoPlayerBinding
    private val vizbeePlayerAdapterHandler by lazy {
        MyVizbeePlayerAdapterHandler(applicationContext.isVizbeeEnabled)
    }
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    private var mHandler: Handler = Handler(Looper.getMainLooper())
    private var mPlayer: ExoPlayer? = null

    private var mStartPosition = -1L

    private var mSdkFactory: ImaSdkFactory? = null
    private var mAdsLoader: AdsLoader? = null
    private var mAdsManager: AdsManager? = null
    private var mIsAdDisplayed = false
    private var mIsAdsInitialized = false

    private val playerListener: MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener =
        object : MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener {
            override fun isContentPlaying(): Boolean {
                return mPlayer?.isPlaying ?: false
            }

            override fun getContentPosition(): Long {
                return mPlayer?.currentPosition ?: 0
            }

            override fun getDuration(): Long {
                return mPlayer?.duration ?: 0
            }

            override fun isAdPlaying(): Boolean {
                return mIsAdDisplayed
            }

            override fun getAdPosition(): Long {
                return mPlayer?.currentPosition ?: 0
            }

            override fun getAdDuration(): Long {
                return mPlayer?.duration ?: 0
            }

            override fun toggleClosedCaptions() {
                // Not implemented
            }

            override fun isClosedCaptioning(): Boolean {
                return false
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 1. Initialize player
        initializeExoPlayer()

        // 2. Initialize MediaSession
        initializeMediaSession()

        // 5. Initialize & request Ads
        initializeAds()
    }

    private fun initializeExoPlayer() {
        // 1. Create a default TrackSelector
        val trackSelector: TrackSelector = DefaultTrackSelector(this)

        // 2. Create a default LoadControl
        val loadControl: LoadControl = DefaultLoadControl()

        // 3. Create the player
        mPlayer = ExoPlayer.Builder(this).setTrackSelector(trackSelector).setLoadControl(loadControl).build()
        mPlayer?.addListener(this)

        // 4. Set up the player view
        binding.exoplayerPlayer.player = mPlayer
        binding.exoplayerPlayer.useController = true
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "ExoPlayerMediaSession")
        mediaSession?.let { session ->
            session.isActive = true

            mediaSessionConnector = MediaSessionConnector(session)
            mediaSessionConnector?.let {
                it.setPlayer(mPlayer)
            }


            session.setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    mPlayer?.play()
                }

                override fun onPause() {
                    mPlayer?.pause()
                }

                override fun onSeekTo(pos: Long) {
                    mPlayer?.seekTo(pos)
                }
            })

            updatePlaybackState()
            updateMediaMetadata()

            mPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    updatePlaybackState()
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateMediaMetadata()
                }
            })
        }
    }

    private fun updatePlaybackState() {
        mPlayer?.let {
            val state = when {
                it.isPlaying -> PlaybackStateCompat.STATE_PLAYING
                it.playbackState == Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                else -> PlaybackStateCompat.STATE_PAUSED
            }

            val playbackState = PlaybackStateCompat.Builder()
                .setState(state, it.currentPosition, it.playbackParameters.speed)
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .build()

            mediaSession?.setPlaybackState(playbackState)
        }
    }

    private fun updateMediaMetadata() {
        val mediaMetadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Sample Video Title")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Sample Artist")
            .build()

        mediaSession?.setMetadata(mediaMetadata)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager?.resume()
        } else if (mPlayer != null) {
            mPlayer?.playWhenReady = true
        }

        super.onResume()
        handleVideoIntent()
    }

    private fun handleVideoIntent() {
        val extras = intent.extras
        if (null != extras && !extras.containsKey("duplicate")) {
            intent.putExtra("duplicate", true)
            var video: Video? = null
            var position = 0L
            if (extras.containsKey("video")) {
                video = extras.getParcelable("video")
            }
            if (extras.containsKey("position")) {
                position = extras.getLong("position")
            }
            if (null != video) {
                prepareVideo(video, position)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        applicationContext.vizbeeAppLifecycleAdapter?.setVideoPlaying(true)
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------
    }

    override fun onStop() {
        super.onStop()

        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        applicationContext.vizbeeAppLifecycleAdapter?.setVideoPlaying(false)
        vizbeePlayerAdapterHandler.resetPlayerAdapter()
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------
        if (null != mPlayer) {
            mPlayer?.release()
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return if (binding.exoplayerPlayer.dispatchMediaKeyEvent(event)) {
            true
        } else super.dispatchKeyEvent(event)
    }

    private fun prepareVideo(video: Video?, position: Long) {
        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        vizbeePlayerAdapterHandler.setPlayerAdapter(video, mediaSession, playerListener)
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------

        // Save start position
        mStartPosition = position
        loadVideo(video, position)
        requestAds(getString(R.string.ad_tag_vmap_pre_roll))
    }

    private fun loadVideo(video: Video?, position: Long) {
        val videoUri = Uri.parse(video?.videoURL)

        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(
                this,
                DefaultHttpDataSource.Factory().setUserAgent(ExoplayerUtils.getUserAgent(this))
            )

        // Produces Extractor instances for parsing the media data.
        val extractorsFactory: ExtractorsFactory = DefaultExtractorsFactory()

        // This is the MediaSource representing the media to be played.
        val videoSource: MediaSource =
            ExoplayerUtils.buildMediaSource(videoUri, dataSourceFactory, mHandler, "", this)

        // Prepare the player with the source.
        mPlayer?.prepare(videoSource)
        mPlayer?.playWhenReady = false
        mPlayer?.addListener(this)
        resumeContent()
    }

    private fun resumeContent() {
        // Seek when start position is available
        if (mAdsLoader == null || !mIsAdDisplayed) {
            if (mStartPosition != -1L) {
                mPlayer?.seekTo(mStartPosition.toLong())
                mStartPosition = -1
            }
            if (mPlayer != null) {
                mPlayer?.playWhenReady = true
            }
            binding.exoplayerPlayer.visibility = View.VISIBLE
        }
    }

    fun onLoadError(error: IOException?) {}

    //-------------------------------------------------------------------------
    // ExoPlayer events
    //-------------------------------------------------------------------------
    fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

    override fun onLoadingChanged(isLoading: Boolean) {}

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playbackState == ExoPlayer.STATE_READY) {
            Log.d(LOG_TAG, "Player state changed: READY, PlayWhenReady = $playWhenReady")
        } else if (playbackState == ExoPlayer.STATE_ENDED) {
            Log.d(LOG_TAG, "Player state changed: ENDED")
            finish()
        } else if (playbackState == ExoPlayer.STATE_BUFFERING) {
            Log.d(LOG_TAG, "Player state changed: BUFFERING")
        } else if (playbackState == ExoPlayer.STATE_IDLE) {
            Log.d(LOG_TAG, "Player state changed: IDLE")
        }
    }

    fun onPlayerError(error: ExoPlaybackException) {
        Log.d(LOG_TAG, "Playback error: " + error.localizedMessage)
    }

    //-------------------------------------------------------------------------
    // Ads
    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------
    // Ads
    //-------------------------------------------------------------------------
    private fun initializeAds() {
        if (mIsAdsInitialized) {
            return
        }
        Log.i(LOG_TAG, "Initializing ads SDK")

        // Create IMA settings
        val imaSdkSettings = ImaSdkFactory.getInstance().createImaSdkSettings()

        // Create an AdsLoader.
        mSdkFactory = ImaSdkFactory.getInstance()
//        mAdsLoader = mSdkFactory.createAdsLoader(this, imaSdkSettings, ImaSdkFactory.createAdDisplayContainer())

        // Add listeners for when ads are loaded and for errors.
        mAdsLoader?.addAdErrorListener(this)
        mAdsLoader?.addAdsLoadedListener(AdsLoadedListener { adsManagerLoadedEvent ->
            Log.i(LOG_TAG, "Ads manager loaded, initializing ads manager ")
            mIsAdsInitialized = true

            // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
            // events for ad playback and errors.
            mAdsManager = adsManagerLoadedEvent.adsManager

            // Attach event and error event listeners.
            mAdsManager?.addAdErrorListener(this@ExoPlayerActivity)
            mAdsManager?.addAdEventListener(this@ExoPlayerActivity)
            mAdsManager?.init()
        })
    }

    /**
     * Request video ads from the given VAST ad tag.
     *
     * @param adTagUrl URL of the ad's VAST XML
     */
    private fun requestAds(adTagUrl: String) {
        Log.i(LOG_TAG, "Requesting ads with tag = $adTagUrl")
        val adDisplayContainer = mSdkFactory!!.createAdDisplayContainer()
        adDisplayContainer.adContainer = (findViewById<View>(R.id.exoplayer_root) as ViewGroup)

        // Create the ads request.
        val request = mSdkFactory!!.createAdsRequest()
        request.adTagUrl = adTagUrl
//        request.setAdDisplayContainer(adDisplayContainer)
        request.contentProgressProvider = ContentProgressProvider {
            if (mIsAdDisplayed || mPlayer == null || mPlayer!!.duration <= 1) {
                VideoProgressUpdate.VIDEO_TIME_NOT_READY
            } else VideoProgressUpdate(
                mPlayer!!.currentPosition,
                mPlayer!!.duration
            )
        }

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader?.requestAds(request)
    }

    override fun onAdError(adErrorEvent: AdErrorEvent) {
        Log.e(LOG_TAG, "Ad error: " + adErrorEvent.error.message)
        resumeContent()
    }

    override fun onAdEvent(adEvent: AdEvent) {
        Log.i(LOG_TAG, "Ad event: " + adEvent.type)
        when (adEvent.type) {
            AdEventType.LOADED ->                 // AdEventType.LOADED will be fired when ads are ready to be played.
                // AdsManager.start() begins ad playback. This method is ignored for VMAP or
                // ad rules playlists, as the SDK will automatically start executing the
                // playlist.
                mAdsManager?.start()

            AdEventType.CONTENT_PAUSE_REQUESTED -> {
                // AdEventType.CONTENT_PAUSE_REQUESTED is fired immediately before a video
                // ad is played.
                mIsAdDisplayed = true
                mPlayer!!.playWhenReady = false
                binding.exoplayerPlayer.visibility = View.GONE
            }

            AdEventType.CONTENT_RESUME_REQUESTED -> {
                // AdEventType.CONTENT_RESUME_REQUESTED is fired when the ad is completed
                // and you should start playing your content.
                mIsAdDisplayed = false
                resumeContent()
            }

            AdEventType.ALL_ADS_COMPLETED -> if (mAdsManager != null) {
                mAdsManager?.destroy()
                mAdsManager = null
            }

            else -> {}
        }
    }

    fun onMediaPeriodCreated(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {}

    fun onMediaPeriodReleased(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {}

    fun onReadingStarted(windowIndex: Int, mediaPeriodId: MediaSource.MediaPeriodId?) {}

    companion object {
        const val LOG_TAG = "ExoPlayerActivity"
    }
}