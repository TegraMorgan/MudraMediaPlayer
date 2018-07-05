package il.co.wearabledevices.mudramediaplayer;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.wear.widget.WearableRecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wearable.android.ble.interfaces.IMudraAPI;
import com.wearable.android.ble.interfaces.IMudraDataListener;
import com.wearable.android.ble.interfaces.IMudraDeviceStatuslListener;

import java.util.List;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.MediaBrowserProvider;
import il.co.wearabledevices.mudramediaplayer.ui.SongsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.utils.LogHelper;

import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_GESTURE;
import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_PROPORTIONAL;
import static il.co.wearabledevices.mudramediaplayer.constants.ENQUEUE_ALBUM;
import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        , SongsFragment.OnSongsListFragmentInteractionListener, MediaBrowserProvider {

    public static final String CURRENT_SONG_RECYCLER_POSITION = "CURRENT_POSITION";
    public static final String CURRENT_ALBUM_SIZE = "CURRENT_ALBUM_SIZE";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ALBUMS_LAYOUT_MARGIN = 0;
    private static final int SONGS_LAYOUT_MARGIN = 74;
    static boolean isPlaying = false;
    static boolean isBinded = false, callbackadded = false;
    private final MediaControllerCompat.Callback mMediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                    /* This section hides or displays playback controls - we don't use it


                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogHelper.d(TAG, "mediaControllerCallback.onPlaybackStateChanged: " +
                                "hiding controls because state is ", state.getState());
                        hidePlaybackControls();
                    }
                    */
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    /* This section hides or shows controls - we don't use it

                    if (shouldShowControls()) {
                        showPlaybackControls();
                    } else {
                        LogHelper.d(TAG, "mediaControllerCallback.onMetadataChanged: " +
                                "hiding controls because metadata is null");
                        hidePlaybackControls();
                    }
                    */
                }
            };
    ImageView playPauseView;
    TextView albums_text;
    private IMudraAPI mIMudraAPI = null;
    IMudraDeviceStatuslListener mMudraDeviceStatusCB = new IMudraDeviceStatuslListener.Stub() {
        @Override
        public void onMudraStatusChanged(int statusType, String deviceAddress) throws RemoteException {
            switch (statusType) {
                case 0:
                    Log.i("INFO", "Device Scan Started");
                    break;
                case 1:
                    Log.i("INFO", "Device Scan Stopped");
                    break;
                case 2:
                    Log.i("INFO", "Device Found" + deviceAddress);
                    connectMudra(deviceAddress);
                    break;
                case 3: {
                    Log.i("INFO", "Device connected");
                    startSNCDataTransmission();
                    break;
                }
                case 4:
                    Log.i("INFO", "Device disconnected");
                    break;
            }
        }
    };
    private Context mainContext;
    IMudraDataListener mMudraDataCB = new IMudraDataListener.Stub() {
        @Override
        public void onMudraDataReady(int dataType, float[] d) throws RemoteException {
            switch (dataType) {
                case DATA_TYPE_GESTURE:
                    String gest = "";
                    if ((d[0] > d[1]) && (d[0] > d[2]) && (d[0] > 0.9)) {
                        gest = "Thumb";
                        Log.i("INFO", "gesture: Thumb");
                    }

                    if ((d[1] > d[0]) && (d[1] > d[2]) && (d[1] > 0.9)) {
                        gest = "Tap";
                        Log.i("INFO", "gesture: Tap");
                    }
                    if ((d[2] > d[0]) && (d[2] > d[1]) && (d[2] > 0.9)) {
                        Log.i("INFO", "gesture: Index");
                        gest = "Index";
                        //Log.i("Gesture","0");
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainContext, "gesutre", Toast.LENGTH_SHORT).show();

                        }
                    });
                    break;
                case DATA_TYPE_PROPORTIONAL:/*if ( d[0] > d[1])
                        Log.i ("INFO", "Tap Proportional:" +d[2]);
                       if ( d[1] > d[0])
                           Log.i ("INFO", "Middle Tap Proportional:" +d[2]);*/
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainContext, "Proportional", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i("Gesture", "1");
                    break;
                case 2:
                    Log.i("INFO", "IMU acc x: " + d[0] + " \nacc Y: " + d[1] + " \nacc Z: " + d[2] + " \nQ W: " + d[3] + " \nQ X: " + d[4] + " \nQ Y: " + d[5] + " \nQ Z: " + d[6]);
                    break;
                default:
                    Log.i("Gesture", d[0] + "gesture detected");
                    break;
            }
        }
    };
    private TextView mTextView;
    private MediaBrowserCompat mMediaBrowser;
    //private String deviceAddress = "";
    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    LogHelper.d(TAG, "onConnected");
                    try {
                        connectToSession(mMediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        LogHelper.e(TAG, e, "could not connect media controller");
                        /* This section hides controls - we don't use it

                        hidePlaybackControls();
                        */
                    }
                }
            };
    private ServiceConnection mConnection = new ServiceConnection() { // Called when the connection with the service is established using getApplicationContext()
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                if (!callbackadded) {
                    Log.i("INFO", "bind SUCCEEDED"); // this gets an instance of the MudraAPI, which we can use to call on the service

                    mIMudraAPI = IMudraAPI.Stub.asInterface(service);
                    Log.i("INFO", "Stub");

                    mIMudraAPI.initMudra(mMudraDeviceStatusCB, mMudraDataCB);
                    Log.i("INFO", "init");

                    callbackadded = true;
                }
            } catch (RemoteException ex) {
                Log.e("ERROR", ex.toString());
            }
        }

        // Called when the connection with the service disconnects unexpectedly
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e("ERROR", "Mudra Service has unexpectedly disconnected");
            mIMudraAPI = null;
        }
    };

    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playPauseView = findViewById(R.id.play_pause);
        mTextView = findViewById(R.id.text);

        //TODO current ambient mode is draining the battery. Make a B/W ambient screen
        setAmbientEnabled(); // Enables Always-on

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        Intent intent = new Intent();
        intent.setAction(IMudraAPI.class.getName());
        intent.setComponent(new ComponentName("com.wearable.android.ble",
                "com.wearable.android.ble.service.BluetoothLeService"));
        getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.i("cooooo", "nnect");
        this.mainContext = this;
    }

    /* MUDRA CONTENT */

    /*              #############################################################*/

    @Override
    protected void onStart() {
        super.onStart();
        mMediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaBrowser.disconnect();
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mMediaControllerCallback);
        onMediaControllerConnected();
    }

    public void startScan() {
        try {
            mIMudraAPI.mudraStartScan();
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    public void stopScan() {
        try {
            mIMudraAPI.mudraStopScan();
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    public void connectMudra(String deviceAddress) {
        try {
            mIMudraAPI.connectMudraDevice(deviceAddress);
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    public void startSNCDataTransmission() {
        try {
            mIMudraAPI.startRawSNCDataTransmission();
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    public void stopSNCDataTransmission() {
        try {
            mIMudraAPI.stopRawSNCDataTransmission();
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    public void disonnectMudra() {
        try {
            mIMudraAPI.disconnectMudraDevice();
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    public void releaseMudra() {
        try {
            mIMudraAPI.releaseMudra();
        } catch (RemoteException ex) {
            Log.e("ERROR:", ex.toString());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO save current queue state to bundle
        if (isPlaying) {
            MediaControllerCompat mediaControllerCompat = MediaControllerCompat.getMediaController(MainActivity.this);
            List<MediaSessionCompat.QueueItem> qi = mediaControllerCompat.getQueue();
            String activeSong = String.valueOf(mediaControllerCompat.getPlaybackState().getActiveQueueItemId());

            Log.v("Tegra", "Looking for song - " + String.valueOf(activeSong));

            int playlistPosition = 0;
            for (MediaSessionCompat.QueueItem song : qi) {

                Log.v("Tegra", "Comparing: " + song.getDescription().getMediaId() + " and " + activeSong);

                if (song.getDescription().getMediaId().equals(activeSong)) {
                    playlistPosition = (int) song.getQueueId();
                }
            }
            SongsFragment songsFragment = (SongsFragment) getFragmentManager().findFragmentByTag(SongsFragment.class.getSimpleName());
            Album album = songsFragment.getAlbum();
            outState.putSerializable(SERIALIZE_ALBUM, album);
            outState.putInt(CURRENT_ALBUM_SIZE, album.SongCount());
            outState.putInt(CURRENT_SONG_RECYCLER_POSITION, playlistPosition);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle sis) {//SavedInstanceState
        super.onRestoreInstanceState(sis);
        if (isPlaying) {
            Album al = (Album) sis.getSerializable(SERIALIZE_ALBUM);
            int pos = sis.getInt(CURRENT_SONG_RECYCLER_POSITION);

            showPlayerButtons();
            showSongsScreen();

            android.app.FragmentManager fm = getFragmentManager();//fragmentManager
            SongsFragment slf = SongsFragment.newInstance(sis.getInt(CURRENT_ALBUM_SIZE), al); //songFragment
            slf.setArguments(sis);
            fm.beginTransaction().replace(R.id.songs_list_container, slf).addToBackStack(null).commit();

            WearableRecyclerView rv = slf.getRecycler();
            rv.scrollToPosition(pos);
        }
    }

    /* ******************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Starting on Resume");

        if (!isBinded) {
            Intent intent = new Intent();
            intent.setAction(IMudraAPI.class.getName());
            intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));

            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            isBinded = true;
        } else {
            getApplicationContext().unbindService(mConnection);
            Intent intent = new Intent();
            intent.setAction(IMudraAPI.class.getName());
            intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));

            getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }

        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission
            Log.v(TAG, "No permission");
            // We want to request permission
            // navigate to another activity and request permission there
            Intent NavToReqPerms = new Intent(MainActivity.this, PermissionRequestActivity.class);
            startActivity(NavToReqPerms);
        } else {
            // We already have permission
            //TODO Tegra Launch this on separate thread in the future
            if (!MediaLibrary.isInitialized()) {
                MediaLibrary.buildMediaLibrary(this);
            }
            hidePlayerButtons();
            showAlbumsScreen();
            android.app.FragmentManager fm = getFragmentManager();
            AlbumsFragment slf = new AlbumsFragment();
            fm.beginTransaction().replace(R.id.songs_list_container, slf).commit();
        }

        //TODO restore queue display
        if (isPlaying) {

        }
    }

    @Override
    public void onAlbumsListFragmentInteraction(Album item) {
        //Show the player buttons upon album selection
        //showPlayerButtons();
        isPlaying = true;
        showPlayerButtons();
        showSongsScreen();
        android.app.FragmentManager fm = getFragmentManager();
        SongsFragment slf = SongsFragment.newInstance(item.getaSongs().size(), item);
        // Create Bundle to be sent to Song List Fragment
        Bundle bdl = new Bundle();
        // put album object in it
        bdl.putSerializable(SERIALIZE_ALBUM, item);
        slf.setArguments(bdl);
        fm.beginTransaction().replace(R.id.songs_list_container, slf).addToBackStack(null).commit();
        MediaControllerCompat.getMediaController(MainActivity.this).adjustVolume(AudioManager.ADJUST_RAISE, 0);
        // Enqueue all the album and play it
        Bundle bndl = new Bundle();
        bndl.putSerializable(SERIALIZE_ALBUM, item);
        MediaControllerCompat.TransportControls mediaController = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
        mediaController.sendCustomAction(ENQUEUE_ALBUM, bndl);
    }

    @Override
    public void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position) {
        Snackbar.make(findViewById(R.id.text), String.valueOf(position), Snackbar.LENGTH_SHORT);
        //Toast.makeText(this, String.valueOf(position), Toast.LENGTH_LONG).show();
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToQueueItem(position);
    }

    public void play_music(View view) {
        view.setBackground(getDrawable(!isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));
        if (!isPlaying) {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls()
                    .play();
        } else {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls()
                    .pause();
        }
        isPlaying = !isPlaying;
        //showAlbumsScreen();
    }

    public void nextSong(View view) {
        goToNextSong();
    }

    private void goToNextSong() {
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToNext();
    }

    public void prevSong(View view) {
        goToPrevSong();
    }

    private void goToPrevSong() {
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToPrevious();
    }

    private void modifyVolume(int direction, int flags) {
        MediaControllerCompat.getMediaController(MainActivity.this).adjustVolume(direction, AudioManager.FLAG_VIBRATE);
    }


    public void scrollDown(View view) {
        boolean a;
        AlbumsFragment albumsFragment = (AlbumsFragment) getFragmentManager().findFragmentByTag(AlbumsFragment.class.getSimpleName());
        if (albumsFragment != null && albumsFragment.isVisible()) {
            for (int i = 0; i < 6; i++) {
                a = albumsFragment.next();
                if (!a)
                    Toast.makeText(this, "adapter is null", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Shop Play Pause Next Prev Buttons
     */
    public void showPlayerButtons() {
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.VISIBLE);
        player_next.setVisibility(View.VISIBLE);
        player_play.setVisibility(View.VISIBLE);
    }

    /**
     * Hide Play Pause Next Prev Buttons
     */
    public void hidePlayerButtons() {
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.INVISIBLE);
        player_next.setVisibility(View.INVISIBLE);
        player_play.setVisibility(View.INVISIBLE);
    }

    public int dpToPx(int sizeInDP) {
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources()
                        .getDisplayMetrics());
        return marginInDp;
    }

    public void showAlbumsScreen() {
        FrameLayout fl = findViewById(R.id.songs_list_container);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fl.getLayoutParams();

        setMargins(fl, lp.leftMargin, dpToPx(ALBUMS_LAYOUT_MARGIN), lp.rightMargin, lp.bottomMargin);
    }

    public void showSongsScreen() {
        FrameLayout fl = findViewById(R.id.songs_list_container);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fl.getLayoutParams();
        setMargins(fl, lp.leftMargin, dpToPx(SONGS_LAYOUT_MARGIN), lp.rightMargin, lp.bottomMargin);
    }

    protected void onMediaControllerConnected() {

        //getBrowseFragment().onConnected();
    }


}
