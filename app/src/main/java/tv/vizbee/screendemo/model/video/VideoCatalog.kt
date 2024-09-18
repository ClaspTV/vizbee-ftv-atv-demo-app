package tv.vizbee.screendemo.model.video

import tv.vizbee.screendemo.R
import tv.vizbee.screendemo.utils.camelCase

class VideoCatalog {
    companion object {
        const val BIG_BUCK_BUNNY = "bigbuck"
        const val TEARS_OF_STEEL = "tears"
        const val SINTEL = "sintel"
        const val ELEPHANTS_DREAM = "elephants"

        private val names = arrayListOf(
            SINTEL,
            BIG_BUCK_BUNNY,
            TEARS_OF_STEEL,
            ELEPHANTS_DREAM
        )
        private val liveVideos = arrayListOf(
            SINTEL
        )
        private val videoUrls = arrayListOf(
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/big_buck_bunny_1080p.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/ElephantsDream.m3u8"
        )
        private val imageUrls = arrayListOf(
            "https://s3.amazonaws.com/vizbee/images/demoapp/sintel.jpg",
            "https://s3.amazonaws.com/vizbee/images/demoapp/sintel.jpg",
            "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fscdn.nflximg.net%2Fimages%2F1140%2F9931140.jpg&f=1&nofb=1",
            "https://s3.amazonaws.com/vizbee/images/demoapp/elephants_dream.jpg"
        )
        private val images = arrayListOf(
            R.drawable.sintel_720x1024,
            R.drawable.bigbuckbunny_720x1024,
            R.drawable.tearsofsteel_720x1024,
            R.drawable.elephantdream_720x1024
        )

        val all: HashMap<String, Video> = HashMap<String, Video>().apply {
            for (i in 0 until names.size) {
                put(
                    names[i], Video(
                        title = names[i].camelCase(),
                        guid = names[i],
                        videoURL = videoUrls[i],
                        imageUrl = imageUrls[i],
                        imageRes = images[i],
                        isLive = isLive(names[i])
                    )
                )
            }
        }

        private fun isLive(name: String): Boolean {
            return liveVideos.contains(name)
        }
    }
}