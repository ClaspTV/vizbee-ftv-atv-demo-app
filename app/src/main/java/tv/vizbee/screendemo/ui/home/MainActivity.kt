package tv.vizbee.screendemo.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import tv.vizbee.screendemo.CastUtil.Companion.handleIntentByCastReceiver
import tv.vizbee.screendemo.databinding.ActivityMainBinding
import tv.vizbee.screendemo.model.video.Video
import tv.vizbee.screendemo.model.video.VideoCatalog
import tv.vizbee.screendemo.ui.video.ExoPlayerActivity
import tv.vizbee.screendemo.vizbee.VizbeeWrapper.Companion.vizbeeAppLifecycleAdapter
import tv.vizbee.screendemo.vizbee.applifecycle.AppReadyModel

class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private var appReadyModel: AppReadyModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Let Vizbee know that the app is ready and set the app ready model

        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        appReadyModel = AppReadyModel(this)
        applicationContext.vizbeeAppLifecycleAdapter?.setAppReady(appReadyModel!!)
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------

        setListeners()
    }

    private fun setListeners() {
        binding.mainSintelImageView.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.SINTEL)
        }

        binding.mainAkamaiLiveImageView.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.AKAMAI_LIVE_STREAM)
        }

        binding.mainTearsOfSteelImageView.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.TEARS_OF_STEEL)
        }

        binding.mainElephantsDreamImageView.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.ELEPHANTS_DREAM)
        }
    }

    override fun onStart() {
        super.onStart()

        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        if (handleIntentByCastReceiver(intent)) {
            return
        }
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent")

        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        if (handleIntentByCastReceiver(intent)) {
            return
        }
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------

        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        handleIntent()
    }

    private fun handleIntent() {
        val extras = intent.extras
        if (null != extras && !extras.containsKey("duplicate")) {
            intent.putExtra("duplicate", true)
            var guid: String? = null
            var position = -1L
            if (extras.containsKey("guid")) {
                guid = extras.getString("guid")
            }
            if (extras.containsKey("position")) {
                position = extras.getLong("position")
            }
            guid?.let { launchExoplayerActivity(it, position) }
        }
    }

    private fun launchExoplayerActivity(guid: String, position: Long = 0) {
        var video = VideoCatalog.all[guid]
        if (null == video) {

            Log.d(TAG, "Video with GUID: $guid is not supported by this demo app, trying mobile deeplink")
            tryMobileDeeplink()
            return
        }

        Log.d(TAG, String.format("Launching ExoPlayerActivity with video: %s @ %d", video?.title, position))
        startActivity(
            Intent(this, ExoPlayerActivity::class.java).apply {
                this.putExtra("video", video)
                this.putExtra("position", position)
            }
        )
    }

    private fun tryMobileDeeplink() {

        val extras = intent.extras
        if (null != extras) {

            val deeplink = extras.getString("deeplink") ?: ""
            val guid = extras.getString("guid") ?: ""
            if (deeplink.isNotEmpty()) {

                val title = extras.getString("title") ?: ""
                val imageUrl = extras.getString("imageUrl") ?: ""
                val isLive = extras.getBoolean("isLive")
                val video = Video(title, guid, deeplink, imageUrl, 0, isLive)

                Log.d(TAG, String.format("MobileDeeplink: Launching ExoPlayerActivity with video: %s", video))
                startActivity(
                    Intent(this, ExoPlayerActivity::class.java).apply {
                        this.putExtra("video", video)
                        this.putExtra("position", 0)
                    }
                )
            } else {
                displayPlayError(guid)
            }
        }
    }

    private fun displayPlayError(guid: String) {
        Toast.makeText(
            this,
            String.format("Video with GUID: %s is not supported by this demo app!", guid),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")

        // Clear app ready model

        // ---------------------------
        // [BEGIN] Vizbee Integration
        // ---------------------------
        applicationContext.vizbeeAppLifecycleAdapter?.clearAppReady()
        appReadyModel = null
        // ---------------------------
        // [END] Vizbee Integration
        // ---------------------------
    }
}