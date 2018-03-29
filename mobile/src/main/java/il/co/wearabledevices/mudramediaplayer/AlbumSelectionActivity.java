package il.co.wearabledevices.mudramediaplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import il.co.wearabledevices.mudramediaplayer.client.MediaBrowserHelper;
import il.co.wearabledevices.mudramediaplayer.client.MediaSeekBar;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.service.MudraMusicService2;

public class AlbumSelectionActivity extends AppCompatActivity implements AlbumAdapter.ListItemClickListener {
    private static final String TAG = AlbumSelectionActivity.class.getSimpleName();
    ArrayList<Album> mAlbums;
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerViewAlbums;
    private MediaLibrary mLibrary;

    private boolean mCurrentState;
    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private ImageView mAlbumArt;
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private ImageView mMediaControlsImage;
    private MediaSeekBar mSeekBarAudio;

    private MediaBrowserHelper mMediaBrowserHelper;

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
            if (mCurrentState) {
                mMediaBrowserHelper.getTransportControls().pause();
            } else {
                mMediaBrowserHelper.getTransportControls().play();
            }


            System.exit(0);
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        });

        mMediaBrowserHelper = new MediaBrowserConnection(this);
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());

        mLibrary = new MediaLibrary(this);
        mAlbums = (ArrayList<Album>) mLibrary.getAlbums();
        Log.v(TAG, "Got " + mAlbums.size() + " albums from media library");
        mAlbums.sort(Comparator.comparing(Album::getaName));

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
    protected void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSeekBarAudio.disconnectController();
        mMediaBrowserHelper.onStop();
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
        Bundle bundle = new Bundle();
        bundle.putSerializable("album", sel);
        mMediaBrowserHelper.getTransportControls().playFromMediaId(sel.getaName(), bundle);
    }

    /* deprecated function */
    //TODO delete this ?
    private MediaControllerCompat.TransportControls getSupportMediaController() {
        MediaControllerCompat.TransportControls res = null;
        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this);
        PlaybackStateCompat stateCompat = controllerCompat.getPlaybackState();
        if (stateCompat != null) {
            res = MediaControllerCompat.getMediaController(AlbumSelectionActivity.this).getTransportControls();
        }
        return res;
    }


    /**
     * Customize the connection to our {@link android.support.v4.media.MediaBrowserServiceCompat}
     * and implement our app specific desires.
     */
    private class MediaBrowserConnection extends MediaBrowserHelper {
        private MediaBrowserConnection(Context context) {
            super(context, MudraMusicService2.class);
        }

        @Override
        protected void onConnected(@NonNull MediaControllerCompat mediaController) {
            mSeekBarAudio.setMediaController(mediaController);
        }

        @Override
        protected void onChildrenLoaded(@NonNull String parentId,
                                        @NonNull List<MediaBrowserCompat.MediaItem> children) {
            super.onChildrenLoaded(parentId, children);

            final MediaControllerCompat mediaController = getMediaController();

            // Queue up all media items for this simple sample.
            for (final MediaBrowserCompat.MediaItem mediaItem : children) {
                mediaController.addQueueItem(mediaItem.getDescription());
            }

            // Call prepare now so pressing play just works.
            mediaController.getTransportControls().prepare();
        }
    }

    /**
     * Implementation of the {@link MediaControllerCompat.Callback} methods we're interested in.
     * <p>
     * Here would also be where one could override
     * {@code onQueueChanged(List<MediaSessionCompat.QueueItem> queue)} to get informed when items
     * are added or removed from the queue. We don't do this here in order to keep the UI
     * simple.
     */
    private class MediaBrowserListener extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {
            mCurrentState = playbackState != null &&
                    playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;
            mMediaControlsImage.setPressed(mCurrentState);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {
            if (mediaMetadata == null) {
                return;
            }
            mTitleTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
            mArtistTextView.setText(
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
            mAlbumArt.setImageBitmap(MediaLibrary.getAlbumBitmap(
                    AlbumSelectionActivity.this,
                    mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            super.onQueueChanged(queue);
        }
    }

}
