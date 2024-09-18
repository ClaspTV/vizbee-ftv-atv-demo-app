package tv.vizbee.screendemo.ui.video

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
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceEventListener
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import tv.vizbee.screendemo.databinding.ActivityExoPlayerBinding
import tv.vizbee.screendemo.model.video.Video
import tv.vizbee.screendemo.utils.ExoplayerUtils
import tv.vizbee.screendemo.vizbee.VizbeeWrapper.Companion.isVizbeeEnabled
import tv.vizbee.screendemo.vizbee.VizbeeWrapper.Companion.vizbeeAppLifecycleAdapter
import tv.vizbee.screendemo.vizbee.video.playback.MyVizbeeMediaSessionCompatPlayerAdapter
import tv.vizbee.screendemo.vizbee.video.playback.MyVizbeePlayerAdapterHandler

class ExoPlayerActivity : AppCompatActivity(), MediaSourceEventListener, Player.Listener {
    private val vizbeePlayerAdapterHandler by lazy {
        MyVizbeePlayerAdapterHandler(applicationContext.isVizbeeEnabled)
    }

    private lateinit var binding: ActivityExoPlayerBinding
    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null
    private var mHandler: Handler = Handler(Looper.getMainLooper())
    private var mPlayer: ExoPlayer? = null
    private var mStartPosition = -1L

    private val playerListener: MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener =
        object : MyVizbeeMediaSessionCompatPlayerAdapter.PlayerListener {
            override fun isContentPlaying() = mPlayer?.isPlaying ?: false
            override fun getContentPosition() = mPlayer?.currentPosition ?: 0
            override fun getDuration() = mPlayer?.duration ?: 0
            override fun isAdPlaying() = false
            override fun getAdPosition() = mPlayer?.currentPosition ?: 0
            override fun getAdDuration() = mPlayer?.duration ?: 0
            override fun toggleClosedCaptions() {}
            override fun isClosedCaptioning() = false
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
        mediaSession = MediaSessionCompat(this, "ExoPlayerMediaSession").apply {
            isActive = true
            mediaSessionConnector = MediaSessionConnector(this).apply {
                setPlayer(mPlayer)
            }
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    mPlayer?.play()
                }

                override fun onPause() {
                    mPlayer?.pause()
                }

                override fun onSeekTo(pos: Long) {
                    mPlayer?.seekTo(pos)
                }

                override fun onStop() {
                    mPlayer?.stop()
                    finish()
                }
            })
            updatePlaybackState()
            updateMediaMetadata()
            mPlayer?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) = updatePlaybackState()
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = updateMediaMetadata()
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
                .setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SEEK_TO)
                .build()
            mediaSession?.setPlaybackState(playbackState)
        }
//            ?: kotlin.run { applicationContext.vizbeeAppLifecycleAdapter?.getAppReadyModel()?.deeplinkManager?.handleDeeplinkFailure() }
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
        if (mPlayer != null) {
            mPlayer?.playWhenReady = true
        }

        super.onResume()
        handleVideoIntent()
    }

    private fun handleVideoIntent() {
        intent.extras?.let { extras ->
            if (!extras.containsKey("duplicate")) {
                intent.putExtra("duplicate", true)
                val video: Video? = extras.getParcelable("video")
                val position: Long = extras.getLong("position", 0L)
                video?.let { prepareVideo(it, position) }
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
        if (mStartPosition != -1L) {
            mPlayer?.seekTo(mStartPosition.toLong())
            mStartPosition = -1
        }
        mPlayer?.playWhenReady = true

        binding.exoplayerPlayer.visibility = View.VISIBLE
    }

    //-------------------------------------------------------------------------
    // ExoPlayer events
    //-------------------------------------------------------------------------

    @Deprecated("Deprecated in Java")
    override fun onLoadingChanged(isLoading: Boolean) {
    }

    @Deprecated("Deprecated in Java")
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

    companion object {
        const val LOG_TAG = "ExoPlayerActivity"
    }
}