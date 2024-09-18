package tv.vizbee.screendemo.model.video

import tv.vizbee.screendemo.R

object VideoCatalog {
    const val ELEPHANTS_DREAM = "elephants"
    const val TEARS_OF_STEEL = "tears"
    const val AKAMAI_LIVE_STREAM = "akamai-live-stream"
    const val SINTEL = "sintel"
    const val BIG_BUCK_BUNNY = "BIG_BUCK_BUNNY"

    private val videoDataList = listOf(
        Video(
            title = "Elephant's Dream",
            guid = ELEPHANTS_DREAM,
            videoURL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/ElephantsDream.m3u8",
            imageUrl = "https://s3.amazonaws.com/vizbee/images/demoapp/elephants_dream.jpg",
            imageRes = R.drawable.elephantdream_720x1024
        ),
        Video(
            title = "Tears of Steel",
            guid = TEARS_OF_STEEL,
            videoURL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8",
            imageUrl = "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fscdn.nflximg.net%2Fimages%2F1140%2F9931140.jpg&f=1&nofb=1",
            imageRes = R.drawable.tearsofsteel_720x1024
        ),
        Video(
            title = "Akamai Live Stream",
            guid = AKAMAI_LIVE_STREAM,
            videoURL = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8",
            imageUrl = "https://images.unsplash.com/photo-1604311795833-25e1d5c128c6?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=927&q=80",
            imageRes = R.drawable.akamailive_720x1024,
            isLive = true
        ),
        Video(
            title = "Sintel",
            guid = SINTEL,
            videoURL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            imageUrl = "https://s3.amazonaws.com/vizbee/images/demoapp/sintel.jpg",
            imageRes = R.drawable.sintel_720x1024,
            isLive = true
        ),
//        Video(
//            title = "Big Buck Bunny",
//            guid = BIG_BUCK_BUNNY,
//            videoURL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/big_buck_bunny_1080p.mp4",
//            imageUrl = "https://s3.amazonaws.com/vizbee/images/demoapp/sintel.jpg",
//            imageRes = R.drawable.bigbuckbunny_720x1024
//        )
    )

    val all: Map<String, Video> = videoDataList.associate { video ->
        video.guid to Video(
            title = video.title,
            guid = video.guid,
            videoURL = video.videoURL,
            imageUrl = video.imageUrl,
            imageRes = video.imageRes,
            isLive = video.isLive
        )
    }
}