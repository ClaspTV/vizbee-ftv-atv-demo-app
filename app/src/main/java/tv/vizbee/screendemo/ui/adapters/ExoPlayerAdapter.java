package tv.vizbee.screendemo.ui.adapters;


import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;

import java.lang.ref.WeakReference;

import tv.vizbee.screen.api.VizbeeStatusCodes;
import tv.vizbee.screen.api.adapter.VizbeePlayerAdapter;
import tv.vizbee.screen.api.messages.PlaybackStatus;
import tv.vizbee.screen.api.messages.VideoStatus;

/**
 * A player adapter for Android's ExoPlayer
 */
public class ExoPlayerAdapter extends VizbeePlayerAdapter {

    private static final String LOG_TAG = "ExoPlayerAdapterV2";

    protected ExoPlayer mExoPlayer;
    private final WeakReference<Activity> activityWeakReference;
    private VideoStatus videoStatus;
    private boolean mVideoStarted;

    //-----------------------------------------------------
    // Constructor
    //-----------------------------------------------------

    public ExoPlayerAdapter(ExoPlayer exoPlayer, Activity activity) {
        this.mExoPlayer = exoPlayer;
        this.activityWeakReference = new WeakReference<>(activity);
        this.videoStatus = new VideoStatus();
    }

    //-----------------------------------------------------
    // Playback commands
    //-----------------------------------------------------

    @Override
    public void play() {
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        mExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seek(long position) {
        mExoPlayer.seekTo(position);
    }

    boolean shouldHoldStopRequest = false;


    @Override
    public void stop(int statusCode) {

        Log.i(LOG_TAG, "Stop called: statusCode = " + statusCode);
        String message = "Stop invoked with status code = " + statusCode
                + " (" + VizbeeStatusCodes.debugString(statusCode) + ")";

        if (null != activityWeakReference) {
            Activity activity = activityWeakReference.get();
            if (null != activity) {
                displayToast(message, activity);

                Log.d(LOG_TAG, "Sending KEYCODE_BACK to dismiss the video player activity");
                activity.runOnUiThread(() -> {

                    KeyEvent k1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK);
                    activity.dispatchKeyEvent(k1);
                    KeyEvent k2 = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK);
                    activity.dispatchKeyEvent(k2);
                });

                activityWeakReference.clear();
            }
        }
    }

    //-----------------------------------------------------
    // Video status
    //-----------------------------------------------------

    @Override
    public VideoStatus getVideoStatus() {

        videoStatus = new VideoStatus();

        // set state
        if (mExoPlayer.getPlaybackState() == ExoPlayer.STATE_BUFFERING) {
            videoStatus.mPlaybackStatus = PlaybackStatus.BUFFERING;

        } else if ((mExoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) && mExoPlayer.getPlayWhenReady()) {
            videoStatus.mPlaybackStatus = PlaybackStatus.PLAYING;
            mVideoStarted = true;

        } else if ((mExoPlayer.getPlaybackState() == ExoPlayer.STATE_READY) && !mExoPlayer.getPlayWhenReady()) {
            // IMPORTANT:
            // downstream logic will change this to better state
            if (mVideoStarted) {
                videoStatus.mPlaybackStatus = PlaybackStatus.PAUSED_BY_USER;

            } else {
                videoStatus.mPlaybackStatus = PlaybackStatus.LOADING;
            }

        } else if (mExoPlayer.getPlaybackState() == ExoPlayer.STATE_ENDED) {
            videoStatus.mPlaybackStatus = PlaybackStatus.FINISHED;
        }

        // set position and duration
        videoStatus.mDuration = (int) mExoPlayer.getDuration();
        videoStatus.mPosition = (int) mExoPlayer.getCurrentPosition();

        return videoStatus;
    }

    private void displayToast(String message, Context context) {
        if (null != context) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }
}
