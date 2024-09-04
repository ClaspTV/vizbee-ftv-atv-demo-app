package tv.vizbee.screendemo.videoview

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import androidx.fragment.app.FragmentActivity
import tv.vizbee.screendemo.databinding.ActivityVideoViewPlayerBinding
import tv.vizbee.screendemo.model.Video

class VideoViewPlayerActivity : FragmentActivity(), MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private lateinit var binding: ActivityVideoViewPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoViewPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val controller = MediaController(this)
        binding.videoViewPlayer.setOnPreparedListener(this)
        binding.videoViewPlayer.setOnInfoListener(this)
        binding.videoViewPlayer.setOnCompletionListener(this)
        binding.videoViewPlayer.setOnErrorListener(this)
        binding.videoViewPlayer.setMediaController(controller)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleVideoIntent()
    }

    private fun handleVideoIntent() {
        val extras = intent.extras
        if (null != extras && !extras.containsKey("duplicate")) {
            intent.putExtra("duplicate", true)
            var video: Video? = null
            var position = 0
            if (extras.containsKey("video")) {
                video = extras.getParcelable("video")
            }
            if (extras.containsKey("position")) {
                position = extras.getInt("position")
            }
            if (null != video) {
                prepareVideo(video, position)
            }
        }
    }

    private fun prepareVideo(video: Video, position: Int) {

//        // ---------------------------
//        // [BEGIN] Vizbee Integration
//        // ---------------------------
//        val videoInfo = VideoInfo(video.guid)
//        val vizbeeVideoAdapter = MediaPlayerControlAdapter(binding.videoViewPlayer)
//        Vizbee.getInstance().setPlayerAdapter(videoInfo, vizbeeVideoAdapter)
//        // ---------------------------
//        // [END] Vizbee Integration
//        // ---------------------------

        binding.videoViewPlayer.setVideoPath(video.videoURL)
    }

    override fun onStop() {
        super.onStop()

//        // ---------------------------
//        // [BEGIN] Vizbee Integration
//        // ---------------------------
//        Vizbee.getInstance().removeVideoAdapter()
//        // ---------------------------
//        // [END] Vizbee Integration
//        // ---------------------------

        binding.videoViewPlayer.stopPlayback()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        mediaPlayer.start()
    }

    override fun onInfo(mediaPlayer: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> binding.videoVideoLoading.visibility = View.VISIBLE
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START, MediaPlayer.MEDIA_INFO_BUFFERING_END -> binding.videoVideoLoading.visibility = View.GONE
        }
        return false
    }

    override fun onCompletion(mediaPlayer: MediaPlayer?) {
        // Exit
        finish()
    }

    override fun onError(mediaPlayer: MediaPlayer?, what: Int, extra: Int): Boolean {
        // Exit
        return true
    }

}