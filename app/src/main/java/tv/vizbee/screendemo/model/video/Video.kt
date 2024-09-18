package tv.vizbee.screendemo.model.video

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.DrawableRes

open class Video : Parcelable {
    var title: String
        private set
    var guid: String
        private set
    var videoURL: String
        private set
    var imageUrl: String
        private set
    var isLive: Boolean = false
        private set

    @get:DrawableRes
    @DrawableRes
    var imageRes: Int
        private set

    constructor(
        title: String,
        guid: String,
        videoURL: String,
        imageUrl: String,
        imageRes: Int,
        isLive: Boolean = false
    ) {
        this.title = title
        this.guid = guid
        this.videoURL = videoURL
        this.imageUrl = imageUrl
        this.imageRes = imageRes
        this.isLive = isLive
    }

    protected constructor(`in`: Parcel) {
        title = `in`.readString()!!
        guid = `in`.readString()!!
        videoURL = `in`.readString()!!
        imageUrl = `in`.readString()!!
        imageRes = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(guid)
        dest.writeString(videoURL)
        dest.writeString(imageUrl)
        dest.writeInt(imageRes)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Video?> = object : Parcelable.Creator<Video?> {
            override fun createFromParcel(`in`: Parcel): Video {
                return Video(`in`)
            }

            override fun newArray(size: Int): Array<Video?> {
                return arrayOfNulls(size)
            }
        }
    }
}