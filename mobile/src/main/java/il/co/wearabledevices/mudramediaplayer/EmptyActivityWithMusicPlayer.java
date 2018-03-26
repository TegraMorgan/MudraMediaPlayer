package il.co.wearabledevices.mudramediaplayer;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;

import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.service.MudraMusicService;

/**
 * Copy this activity if you need to create new activity with controls of music service
 */

public class EmptyActivityWithMusicPlayer extends AppCompatActivity {

    private MudraMusicService musicSrv;
    private int mCurrentState;
    private MediaControllerCompat.Callback mControllerCallback = new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    mCurrentState = PlaybackStateCompat.STATE_PAUSED;
                    break;
                }
            }
        }
    };
    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;
    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mMediaControllerCompat = new MediaControllerCompat(EmptyActivityWithMusicPlayer.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mControllerCallback);
                MediaControllerCompat.setMediaController(EmptyActivityWithMusicPlayer.this, mMediaControllerCompat);
            } catch (RemoteException e) {

            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_with_music_player);
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    protected void StopPlayback() {

    }

    protected void StartPlayback() {
        /* Change toPlay according to your need */
        Playlist toPlay = new Playlist();

    }
}
