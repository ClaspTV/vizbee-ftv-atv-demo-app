package tv.vizbee.screendemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import tv.vizbee.screendemo.CastUtil.Companion.handleIntentByCastReceiver
import tv.vizbee.screendemo.databinding.ActivityMainBinding
import tv.vizbee.screendemo.exoplayer.ExoPlayerActivity
import tv.vizbee.screendemo.model.VideoCatalog
import tv.vizbee.screendemo.videoview.VideoViewPlayerActivity
import tv.vizbee.screendemo.vizbee.VizbeeWrapper
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

        binding.mainVideoOneImage.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.SINTEL)
        }

//        binding.mainVideoTwoImage.setOnClickListener {
//            launchVideoViewPlayerActivity(VideoCatalog.BIG_BUCK_BUNNY)
//        }

        binding.mainVideoThreeImage.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.TEARS_OF_STEEL)
        }

        binding.mainVideoFourImage.setOnClickListener {
            launchExoplayerActivity(VideoCatalog.ELEPHANTS_DREAM)
        }


        // Let Vizbee know that the app is ready and set the app ready model
        appReadyModel = AppReadyModel(this)
        VizbeeWrapper.setAppReady(appReadyModel!!)
    }

    override fun onStart() {
        super.onStart()

        // ---------------------------
        // Begin SDK Integration
        // ---------------------------
        if (handleIntentByCastReceiver(intent)) {
            return
        }
        // ---------------------------
        // End SDK Integration
        // ---------------------------
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        // ---------------------------
        // Begin SDK Integration
        // ---------------------------
        if (handleIntentByCastReceiver(getIntent())) {
            return
        }
        // ---------------------------
        // End SDK Integration
        // ---------------------------

        setIntent(intent)
        Log.i(TAG, "onNewIntent ${printIntentContents(intent!!)}")
    }

    override fun onResume() {
        super.onResume()
        handleIntent()
    }

    private fun handleIntent() {
        Log.i(TAG, "HandleIntent ${printIntentContents(intent)}")
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

    fun printIntentContents(intent: Intent): String? {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Action: ").append(intent.action).append("\n")
        stringBuilder.append("Data: ").append(intent.data).append("\n")
        stringBuilder.append("Type: ").append(intent.type).append("\n")
        stringBuilder.append("Extras:\n")
        val extras = intent.extras
        if (extras != null) {
            for (key in extras.keySet()) {
                val value = extras[key]
                stringBuilder.append("   ").append(key).append(": ").append(value).append("\n")
            }
        }
        stringBuilder.append("Flags: ").append(intent.flags).append("\n")
        return stringBuilder.toString()
    }

    private fun launchVideoViewPlayerActivity(guid: String, position: Int = 0) {
        val video = VideoCatalog.all[guid]
        video?.let {
            Log.d(
                TAG,
                java.lang.String.format("Launching VideoViewPlayerActivity with video: %s @ %d", it.title, position)
            )
            startActivity(
                Intent(this, VideoViewPlayerActivity::class.java)
                    .putExtra("video", video)
                    .putExtra("position", position)
            )
        } ?: kotlin.run {
            displayPlayError(guid)
        }
    }

    private fun launchExoplayerActivity(guid: String, position: Long = 0) {
        var video = VideoCatalog.all[guid]
        if (null == video) {
            displayPlayError(guid)
            video = VideoCatalog.all[VideoCatalog.ELEPHANTS_DREAM]
        }

        Log.d(TAG, java.lang.String.format("Launching ExoPlayerActivity with video: %s @ %d", video?.title, position))
        startActivity(
            Intent(this, ExoPlayerActivity::class.java)
                .putExtra("video", video)
                .putExtra("position", position)
        )
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
        VizbeeWrapper.appLifecycleAdapter.clearAppReady()
        appReadyModel = null
    }
}