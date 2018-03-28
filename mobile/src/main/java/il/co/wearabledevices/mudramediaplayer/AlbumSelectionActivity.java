package il.co.wearabledevices.mudramediaplayer;

import android.content.ComponentName;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.service.MudraMusicService2;

public class AlbumSelectionActivity extends AppCompatActivity implements AlbumAdapter.ListItemClickListener {
    private static final String TAG = AlbumSelectionActivity.class.getSimpleName();
    ArrayList<Album> mAlbums;
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerViewAlbums;
    private MediaLibrary mLibrary;

    private int mCurrentState;
    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

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

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                mMediaControllerCompat = new MediaControllerCompat(AlbumSelectionActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mControllerCallback);
                MediaControllerCompat.setMediaController(AlbumSelectionActivity.this, mMediaControllerCompat);

                // Sync existing MediaSession state to the UI.
                mControllerCallback.onMetadataChanged(mMediaControllerCompat.getMetadata());
                mControllerCallback.onPlaybackStateChanged(
                        mMediaControllerCompat.getPlaybackState());
            } catch (RemoteException e) {

            }
        }
    };


    @Override
    protected void onStart() {
        super.onStart();

    }

    @SuppressWarnings("Convert2Diamond")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_selection);
        Toolbar toolbar = findViewById(R.id.tb_album_selectoin);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            //TODO floating button action
            switch (mCurrentState) {
                case PlaybackStateCompat.STATE_PLAYING:

                    break;
                default:
                    break;
            }

            System.exit(0);
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        });

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MudraMusicService2.class), mMediaBrowserCompatConnectionCallback, getIntent().getExtras());
        mMediaBrowserCompat.connect();
        /* This is an example how to use MediaLibrary class */

        /* mLibrary - Global variable of MediaLibrary class */
        /* Initialize new media library with default root path (music) */
        mLibrary = new MediaLibrary(this);
        Log.v(TAG, "Library initialized");
        /* get the albums from the media library */
        mAlbums = (ArrayList<Album>) mLibrary.getAlbums();
        Log.v(TAG, "Got " + mAlbums.size() + " albums from media library");
        /* sort the albums by album name */
        mAlbums.sort(Comparator.comparing(Album::getaName));

        /* end of MediaLibrary example */

        // set the recycler
        recyclerViewAlbums = findViewById(R.id.rv_albums);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAlbums.setLayoutManager(layoutManager);
        recyclerViewAlbums.setHasFixedSize(true);
        // instantiate the adapter with number of albums
        mAdapter = new AlbumAdapter(mAlbums, this);
        recyclerViewAlbums.setAdapter(mAdapter);

    }


    @Override
    protected void onDestroy() {
        Log.v(TAG, "OnDestroy launched");

        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this);
        PlaybackStateCompat stateCompat = controllerCompat.getPlaybackState();
        if (stateCompat != null && stateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
            MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this).getTransportControls();
            controls.pause();
        }

        mMediaBrowserCompat.disconnect();

        super.onDestroy();
    }

    @Override
    public void onListItemClick(int cii) {
        Album sel = mAlbums.get(cii);
        PlaybackStateCompat stateCompat = mMediaControllerCompat.getPlaybackState();
        Log.v(TAG, "Got state : " + ((stateCompat != null) ? "true" : "false"));
        Bundle bundle = new Bundle();
        bundle.putSerializable("album", sel);
        //if (stateCompat != null) {
        MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this).getTransportControls();
        Log.v(TAG, "Got controller : " + ((controls != null) ? "true" : "false"));
        switch (mCurrentState) {
            case PlaybackStateCompat.STATE_PLAYING:
            case PlaybackStateCompat.STATE_BUFFERING:
                controls.pause();
                mCurrentState = PlaybackStateCompat.STATE_PAUSED;
                break;
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                controls.playFromMediaId(sel.getaName(), bundle);
                mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                break;
            default:
                Log.d(TAG, "Unhandled click");
        }
        //}
        // old method
        /*
        musicSrv.setList(new Playlist(sel));
        musicSrv.playSong();
        */
    }

    /* deprecated function */
    private MediaControllerCompat.TransportControls getSupportMediaController() {
        MediaControllerCompat.TransportControls res = null;
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this);
        PlaybackStateCompat stateCompat = controllerCompat.getPlaybackState();
        if (stateCompat != null) {
            res = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this).getTransportControls();
        }
        return res;
    }
}
