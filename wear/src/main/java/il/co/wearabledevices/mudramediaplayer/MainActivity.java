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
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
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
    /*  Unfortunately, we have been unable to get playback state
        directly from the music service
        so we have made our own isPlaying boolean  */
    static boolean isPlaying = false;
    static boolean isMudraBinded = false, mudraCallbackAdded = false;
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
    public View a1;
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
    public View a2;

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

    public View a3;
    IMudraDataListener mMudraDataCB = new IMudraDataListener.Stub() {
        @Override
        public void onMudraDataReady(int dataType, float[] data) throws RemoteException {
            switch (dataType) {
                case DATA_TYPE_GESTURE:
                    String gest = "";
                    if ((data[0] > data[1]) && (data[0] > data[2]) && (data[0] > 0.9)) {
                        gest = "Thumb";
                        Log.i("INFO", "gesture: Thumb");
                        // Previous song
                        try {
                            runOnUiThread(() ->
                            {
                                MediaControllerCompat a = MediaControllerCompat.getMediaController(MainActivity.this);
                                Log.v("Tegra", "Got media controller? : " + String.valueOf(a != null));
                                if (a != null) {
                                    MediaControllerCompat.TransportControls b = a.getTransportControls();
                                    Log.v("Tegra", "Got transport controller? : " + String.valueOf(b != null));
                                    if (b != null) {
                                        String c = String.valueOf(a.getQueueTitle());
                                        Log.v("Tegra", "Queue title is : " + c);
                                        if (!c.equals("null")) {
                                            Log.v("Tegra", "Music is playing? : " + String.valueOf(isPlaying));
                                            prevSong();
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            if (RemoteException.class == e.getClass()) {
                                Log.e("Tegra", e.toString());
                            }
                            Log.e("Tegra", e.toString());
                        }
                    }

                    if ((data[1] > data[0]) && (data[1] > data[2]) && (data[1] > 0.9)) {
                        gest = "Tap";
                        Log.i("INFO", "gesture: Tap");
                        // Play or pause
                        try {
                            runOnUiThread(() ->
                            {
                                MediaControllerCompat a = MediaControllerCompat.getMediaController(MainActivity.this);
                                Log.v("Tegra", "Got media controller? : " + String.valueOf(a != null));
                                if (a != null) {
                                    MediaControllerCompat.TransportControls b = a.getTransportControls();
                                    Log.v("Tegra", "Got transport controller? : " + String.valueOf(b != null));
                                    if (b != null) {
                                        String c = String.valueOf(a.getQueueTitle());
                                        Log.v("Tegra", "Queue title is : " + c);
                                        if (!c.equals("null")) {
                                            Log.v("Tegra", "Music is playing? : " + String.valueOf(isPlaying));
                                            play_music();
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            if (RemoteException.class == e.getClass()) {
                                Log.e("Tegra", e.toString());
                            }
                            Log.e("Tegra", e.toString());
                        }
                    }
                    if ((data[2] > data[0]) && (data[2] > data[1]) && (data[2] > 0.9)) {
                        Log.i("INFO", "gesture: Index");
                        gest = "Index";
                        // Next song
                        try {
                            runOnUiThread(() ->
                            {
                                MediaControllerCompat a = MediaControllerCompat.getMediaController(MainActivity.this);
                                Log.v("Tegra", "Got media controller? : " + String.valueOf(a != null));
                                if (a != null) {
                                    MediaControllerCompat.TransportControls b = a.getTransportControls();
                                    Log.v("Tegra", "Got transport controller? : " + String.valueOf(b != null));
                                    if (b != null) {
                                        String c = String.valueOf(a.getQueueTitle());
                                        Log.v("Tegra", "Queue title is : " + c);
                                        if (!c.equals("null")) {
                                            Log.v("Tegra", "Music is playing? : " + String.valueOf(isPlaying));
                                            nextSong();
                                        }
                                    }
                                }
                            });
                        } catch (Exception e) {
                            if (RemoteException.class == e.getClass()) {
                                Log.e("Tegra", e.toString());
                            }
                            Log.e("Tegra", e.toString());
                        }
                        playPauseView.postInvalidate();
                    }
                    //No need for this anymore
                    //runOnUiThread(() -> Toast.makeText(mainContext, "Gesture", Toast.LENGTH_SHORT).show());
                    break;
                case DATA_TYPE_PROPORTIONAL:/*if ( data[0] > data[1])
                        Log.i ("INFO", "Tap Proportional:" +data[2]);
                       if ( data[1] > data[0])
                           Log.i ("INFO", "Middle Tap Proportional:" +data[2]);*/
                    runOnUiThread(() -> Toast.makeText(mainContext, "Proportional", Toast.LENGTH_SHORT).show());
                    Log.i("Gesture", "1");
                    break;
                case 2:
                    Log.i("INFO", "IMU acc x: " + data[0] + " \nacc Y: " + data[1] + " \nacc Z: " + data[2] + " \nQ W: " + data[3] + " \nQ X: " + data[4] + " \nQ Y: " + data[5] + " \nQ Z: " + data[6]);
                    break;
                default:
                    Log.i("Gesture", data[0] + "gesture detected");
                    break;
            }
        }
    };
    private ServiceConnection mMudraConnection = new ServiceConnection() { // Called when the connection with the service is established using getApplicationContext()
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                if (!mudraCallbackAdded) {
                    Log.i("INFO", "bind SUCCEEDED"); // this gets an instance of the MudraAPI, which we can use to call on the service

                    mIMudraAPI = IMudraAPI.Stub.asInterface(service);
                    Log.i("INFO", "Stub");

                    mIMudraAPI.initMudra(mMudraDeviceStatusCB, mMudraDataCB);
                    Log.i("INFO", "init");

                    mudraCallbackAdded = true;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playPauseView = findViewById(R.id.play_pause);
        mTextView = findViewById(R.id.text);
        /*
        By default all ViewGroup sub-classes do not call their onDraw method, you should enable it by calling setWillNotDraw(false)
        http://developer.android.com/reference/android/view/View.html#setWillNotDraw%28boolean%29
         */

        playPauseView.setWillNotDraw(false);
        a1 = findViewById(R.id.test1);
        a1.setWillNotDraw(false);
        a2 = findViewById(R.id.main_screen);
        a2.setWillNotDraw(false);
        a3 = findViewById(R.id.above);
        a3.setWillNotDraw(false);

        //TODO current ambient mode is draining the battery. Make a B/W ambient screen
        setAmbientEnabled(); // Enables Always-on

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        Intent intent = new Intent();
        intent.setAction(IMudraAPI.class.getName());
        intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));

        getApplicationContext().bindService(intent, mMudraConnection, Context.BIND_AUTO_CREATE);
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
    protected void onDestroy() {
        super.onDestroy();
        //Added by Tegra. When the application stops, we want to close Mudra channel
        releaseMudra();
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

    /* ******************************************************************************/

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Starting on Resume");

        if (!isMudraBinded) {
            Intent intent = new Intent();
            intent.setAction(IMudraAPI.class.getName());
            intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));

            getApplicationContext().bindService(intent, mMudraConnection, Context.BIND_AUTO_CREATE);
            isMudraBinded = true;
        } else {
            getApplicationContext().unbindService(mMudraConnection);
            Intent intent = new Intent();
            intent.setAction(IMudraAPI.class.getName());
            intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));

            getApplicationContext().bindService(intent, mMudraConnection, Context.BIND_AUTO_CREATE);
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
        MediaControllerCompat.TransportControls transportControls = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
        transportControls.sendCustomAction(ENQUEUE_ALBUM, bndl);
    }

    @Override
    public void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position) {
        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_LONG).show();
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToQueueItem(position);
        isPlaying = true;
    }

    public void play_music(View view) {
        view.setBackground(getDrawable(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));
        if (!isPlaying) {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
        } else {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
        }
        isPlaying = !isPlaying;
        view.invalidate();
        //showAlbumsScreen();
    }

    public void play_music() {
        playPauseView.setBackground(getDrawable(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));
        if (!isPlaying) {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
        } else {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
        }
        isPlaying = !isPlaying;
        tempsolution(playPauseView);
    }

    public void nextSong(View view) {
        nextSong();
    }

    private void nextSong() {
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToNext();
        playPauseView.setBackground(getDrawable(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));
        tempsolution(playPauseView);
        isPlaying = true;
    }

    public void prevSong(View view) {
        prevSong();
    }

    private void prevSong() {
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToPrevious();
        playPauseView.setBackground(getDrawable(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));
        tempsolution(playPauseView);
        isPlaying = true;
    }

    public void tempsolution(View v) {
        a1.invalidate();
        a2.invalidate();
        a3.invalidate();
        v.invalidate();

    }

    private void modifyVolume(int direction, int flags) {
        //TODO show ui when state can be saved
        //MediaControllerCompat.getMediaController(MainActivity.this).adjustVolume(direction, AudioManager.FLAG_SHOW_UI);
        MediaControllerCompat.getMediaController(MainActivity.this).adjustVolume(direction, 0);
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
