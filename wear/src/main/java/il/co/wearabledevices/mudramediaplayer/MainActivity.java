package il.co.wearabledevices.mudramediaplayer;


import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.view.MotionEvent;
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
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.MediaBrowserProvider;
import il.co.wearabledevices.mudramediaplayer.ui.SongsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.utils.LogHelper;

import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_GESTURE;
import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_PROPORTIONAL;
import static il.co.wearabledevices.mudramediaplayer.constants.ENQUEUE_ALBUM;
import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

@SuppressWarnings("SpellCheckingInspection")
public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        , SongsFragment.OnSongsListFragmentInteractionListener, MediaBrowserProvider {

    private static final String TAG = MainActivity.class.getSimpleName();
    //#endregion

    //#region Variables

    /*
        Unfortunately, we have been unable to get playback state directly
        from the music service so we have made our own isPlaying boolean
    */
    static boolean isPlaying = false, isMudraBinded = false, mudraCallbackAdded = false, VolumeUp = false;
    private static int mudraSmoother;
    ImageView playPauseView;
    private Context mainContext;
    private IMudraAPI mIMudraAPI = null;
    Long lastPressureOccurence;
    private int currentAlbumPosition = 0;
    private static long currentTime, lastTime, firstMeasure, a4;
    private TextView mTextView;
    private MediaBrowserCompat mMediaBrowser;
    private static Album nowPlaying;
    private AlbumsFragment mAlbumsFragment;
    private SongsFragment mSongsFragment;
    private int currentPlayingSongPosition = 0;
    private String currentScreen = constants.VIEW_ALBUMS;
    //#endregion

    //#region Media controller and everything in it

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

    //#endregion

    //#region MUDRA CONTENT
    /**
     * This Listener decides what actions to take when any gesture is recognized
     */
    IMudraDataListener mMudraDataCB = new IMudraDataListener.Stub() {
        @Override
        public void onMudraDataReady(int dataType, float[] data) throws RemoteException {
            switch (dataType) {
                case DATA_TYPE_GESTURE:
                    if ((data[0] > data[1]) && (data[0] > data[2]) && (data[0] > 0.9)) {
                        Log.i("INFO", "gesture: Thumb");
                        // Previous song
                        try {
                            runOnUiThread(() -> {
                                Log.i("Mudra interaction", "Thumb " + currentScreen);
                                if (currentScreen.equals(constants.VIEW_SONGS))
                                    prevSong(constants.USING_MUDRA);
                                else {
                                    prevAlbum();
                                }
                            });
                        } catch (Exception e) {
                            Log.e("Tegra", e.toString());
                        }
                    }
                    if ((data[1] > data[0]) && (data[1] > data[2]) && (data[1] > 0.9)) {
                        Log.i("INFO", "gesture: Tap");
                        // Play or pause
                        try {
                            runOnUiThread(() -> {
                                Log.i("Mudra interaction", "Tap " + currentScreen);
                                if (currentScreen.equals(constants.VIEW_SONGS))
                                    play_music();
                                else
                                    clickAlbum();
                            });
                        } catch (Exception e) {
                            Log.e("Tegra", e.toString());
                        }
                    }
                    if ((data[2] > data[0]) && (data[2] > data[1]) && (data[2] > 0.9)) {
                        Log.i("INFO", "gesture: Index");
                        // Next song
                        try {
                            runOnUiThread(() -> {
                                Log.i("Mudra interaction", "Index " + currentScreen);
                                if (currentScreen.equals(constants.VIEW_SONGS))
                                    nextSong(constants.USING_MUDRA);
                                else
                                    nextAlbum();
                            });
                        } catch (Exception e) {
                            Log.e("Tegra", e.toString());
                        }
                    }
                    break;
                case DATA_TYPE_PROPORTIONAL:

                    /* if ( data[0] > data[1]) Log.i ("INFO", "Tap Proportional:" +data[2]);
                       if ( data[1] > data[0]) Log.i ("INFO", "Middle Tap Proportional:" +data[2]);
                    */

                    runOnUiThread(() ->
                    {
                        Log.v("Tegra", "Proportional strength : " + String.valueOf(data[2]));
                        Log.i("Mudra interaction", "Proportional");
                        if (isPlaying) {                         // if music is playing pressure controls volume
                            if (data[2] > constants.MUDRA_VOLUME_PRESSURE_SENSITIVITY) {
                                // Measure time from last proportional gesture
                                long del = System.currentTimeMillis() - lastPressureOccurence;
                                // If there was no gesture for a long time - reset smoother
                                if (del > constants.VOLUME_DIRECTION_FLIP_DELAY) {
                                    mudraSmoother = 0;
                                    VolumeUp = !VolumeUp;
                                    String msg = VolumeUp ? "Volume Up" : "Volume Down";
                                    Toast.makeText(mainContext, msg, Toast.LENGTH_SHORT).show();
                                    //Measure the proportional strength
                                }
                                /* Only one of three volume change commands works - change is too quick */
                                if (mudraSmoother % constants.MUDRA_SMOOTH_FACTOR == 0) {
                                    int direction = VolumeUp ? 1 : -1;
                                    modifyVolume(direction, 0);
                                    lastPressureOccurence = System.currentTimeMillis();
                                }
                                mudraSmoother = (mudraSmoother + 1) % constants.MUDRA_SMOOTH_FACTOR;
                            }
                        } else {
                            // if music is not playing long pressure is back button
                            if (data[2] > constants.MUDRA_VOLUME_PRESSURE_SENSITIVITY) {  //only react if the pressure is strong enough
                                currentTime = System.currentTimeMillis();
                                if (currentTime - lastTime > 400) {
                                    lastTime = currentTime;
                                    firstMeasure = currentTime;
                                } else {
                                    if (firstMeasure - currentTime > constants.BACK_BUTTON_DELAY) {
                                        // Pressure was for 1.5 seconds - we can act now
                                        if (currentScreen.equals(constants.VIEW_SONGS)) {
                                            switchToAlbumView();
                                        } else {
                                            // We can exit application here if we want
                                        }
                                    } else {
                                        // continue waiting
                                        lastTime = currentTime;
                                    }
                                }

                            }

                        }
                    });
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

    //#endregion

    //#region Main lifecycle functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lastPressureOccurence = System.currentTimeMillis();
        setContentView(R.layout.activity_main);
        playPauseView = findViewById(R.id.play_pause);
        findViewById(R.id.player_next).setOnTouchListener(clickEffect);
        findViewById(R.id.player_prev).setOnTouchListener(clickEffect);
        playPauseView.setOnTouchListener(clickEffect);
        mTextView = findViewById(R.id.text);
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
        this.mainContext = this;
    }

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
            // Tegra this our Main function delete this line when finished

            if (!isPlaying) {
                switchToAlbumView();
            }
        }
    }

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
        // FIXME Next line should be removed in production
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
        isPlaying = false;
        updatePlayButton(playPauseView);
        super.onStop();
        mMediaBrowser.disconnect();
    }

    protected void onMediaControllerConnected() {

        //getBrowseFragment().onConnected();
    }

    //#endregion

    //#region Fragment interaction functions

    /**
     * Fires when user selects album to play
     *
     * @param item Album the user has selected
     */
    @Override
    public void onAlbumsListFragmentInteraction(AlbumsAdapter.ViewHolder item, int position) {
        currentPlayingSongPosition = 0;
        /* --- Local variables preparation and update --- */
        isPlaying = true;                                       // Set play state as playing
        nowPlaying = item.mItem;                                      // Save inflated playlist in a static variable
        switchToSongView(item.mItem);
        updatePlayButton(playPauseView);
        /* --- Music Service controls --- */
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(MainActivity.this);
        MediaControllerCompat.TransportControls transportControls = controller.getTransportControls();
        jumpstartVolume(controller);                                    // Boost initial volume
        Bundle albumBundle = new Bundle();                              // Create Bundle to be sent to Queue Manager
        albumBundle.putSerializable(SERIALIZE_ALBUM, item.mItem);             // Put album object in it
        transportControls.sendCustomAction(ENQUEUE_ALBUM, albumBundle); // Enqueue all the album and play it
    }


    public static void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    @Override
    public void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position) {
        Toast.makeText(this, "Playing : " + nowPlaying.getAlbumSongs().get(position).getTitle(), Toast.LENGTH_LONG).show();
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToQueueItem(position);
        //save current playing song position
        currentPlayingSongPosition = position;
        Log.i("current position (click)", currentPlayingSongPosition + "");
        //put that song in the center of the screen
        mSongsFragment.scrollToPos(position, true);
        mSongsFragment.getRecycler().getAdapter().notifyDataSetChanged();
        isPlaying = true;
        updatePlayButton(playPauseView);
    }

    public void play_music(View view) {
        play_music();
    }

    public void play_music() {
        if (!isPlaying) {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
        } else {
            MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
        }
        isPlaying = !isPlaying;
        updatePlayButton(playPauseView);
        String msg = isPlaying ? "Playing" : "Paused";
        Toast.makeText(mainContext, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Switch to next album - Mudra usage only
     */
    public void nextAlbum() {
        int _albumsCount = mAlbumsFragment.getRecycler().getChildCount();
        if (currentAlbumPosition < _albumsCount - 1) {
            currentAlbumPosition += 1;

            //put the next song in the center of the screen
            mAlbumsFragment.scrollToPos(currentAlbumPosition, true);
            Log.i("current position", currentAlbumPosition + "");
            mAlbumsFragment.getRecycler().getAdapter().notifyDataSetChanged();

        } else {
            Log.i("current position", "reached the end");
        }

    }

    /**
     * Switch to previous album - Mudra usage only
     */
    public void prevAlbum() {
        if (currentAlbumPosition > 0) {
            currentAlbumPosition -= 1;

            //put the next song in the center of the screen
            mAlbumsFragment.scrollToPos(currentAlbumPosition, true);
            Log.i("current position", currentAlbumPosition + "");
            mAlbumsFragment.getRecycler().getAdapter().notifyDataSetChanged();

        } else {
            Log.i("current position", "reached the beginning");
        }

    }

    /**
     * Select album - Mudra usage only
     */
    public void clickAlbum() {
        mAlbumsFragment.getRecycler().getChildAt(mAlbumsFragment.getCurrentItem()).performClick();
    }

    public void nextSong(View view) {
        nextSong(!constants.USING_MUDRA);
    }

    private void nextSong(boolean usingMudra) {
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToNext();
        isPlaying = true;
        updatePlayButton(playPauseView);
        //Toast.makeText(mainContext, "Next", Toast.LENGTH_SHORT).show();

        if (nowPlaying == null)
            return;

        //check if we're inside the safe zone
        int _songsCount = nowPlaying.getAlbumSongs().size();

        if (currentPlayingSongPosition < _songsCount - 1) {
            currentPlayingSongPosition += 1;
            //check if the current object is back and if we're no at the end of the list
            if (currentPlayingSongPosition < _songsCount - 1) {
                currentPlayingSongPosition += 1;
            }
            //put the next song in the center of the screen
            mSongsFragment.scrollToPos(currentPlayingSongPosition, true);
            Log.i("current position", currentPlayingSongPosition + "");
            mSongsFragment.getRecycler().getAdapter().notifyDataSetChanged();

        } else {
            Log.i("current position", "reached the end");
        }

    }

    public void prevSong(View view) {
        prevSong(!constants.USING_MUDRA);
    }

    private void prevSong(boolean usingMudra) {
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().skipToPrevious();
        isPlaying = true;
        updatePlayButton(playPauseView);
        if (nowPlaying == null)
            return;

        //check if we're inside the safe zone

        if (currentPlayingSongPosition > 0) {
            currentPlayingSongPosition -= 1;
            //check if the current object is back and if we're no at the end of the list
            if (currentPlayingSongPosition > 0) {
                currentPlayingSongPosition -= 1;
            }
            //put the next song in the center of the screen
            mSongsFragment.scrollToPos(currentPlayingSongPosition, true);
            Log.i("current position", currentPlayingSongPosition + "");
            mSongsFragment.getRecycler().getAdapter().notifyDataSetChanged();

        } else {
            Log.i("current position", "reached the beginning");
        }
        //Toast.makeText(mainContext, "Prev", Toast.LENGTH_SHORT).show();
    }

    public void updatePlayButton(View v) {
        v.invalidate();
        v.setBackground(getDrawable(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));
    }

    /**
     * Change playback volume
     *
     * @param direction -1 to lower, 1 to raise. Anything else will be discarded.
     * @param flags     AudioManager.FLAGS (not used yet)
     */
    @SuppressWarnings({"SameParameterValue", "unused"})
    private void modifyVolume(int direction, int flags) {
        //TODO show ui when state can be saved
        //MediaControllerCompat.getMediaController(MainActivity.this).adjustVolume(direction, AudioManager.FLAG_SHOW_UI);
        if (direction == -1 || direction == 1)
            MediaControllerCompat.getMediaController(MainActivity.this).adjustVolume(direction, flags);
    }


    /**
     * Shop Play Pause Next Prev Buttons
     */
    public void showPlayerButtons() {
        /*Hide the list for now - for better paging (not the best thing yet)*/
        findViewById(R.id.songs_list_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.songs_list_container).invalidate();

        TextView albums_text = findViewById(R.id.player_albums_text);
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        albums_text.setVisibility(View.INVISIBLE);
        albums_text.invalidate();
        player_prev.setVisibility(View.VISIBLE);
        player_next.setVisibility(View.VISIBLE);
        player_play.setVisibility(View.VISIBLE);

    }

    /**
     * Hide Play Pause Next Prev Buttons
     */
    public void hidePlayerButtons() {
        /*Hide the list for now - for better paging (not the best thing yet)*/
        //findViewById(R.id.songs_list_container).setVisibility(View.INVISIBLE);
        //findViewById(R.id.songs_list_container).invalidate();
        TextView albums_text = findViewById(R.id.player_albums_text);
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.INVISIBLE);
        player_next.setVisibility(View.INVISIBLE);
        player_play.setVisibility(View.INVISIBLE);
        albums_text.setVisibility(View.VISIBLE);

    }

    /**
     * Convert DP to pixels
     *
     * @param sizeInDP Element size in DP
     * @return Element size in Pixels
     */
    public int dpToPx(int sizeInDP) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources().getDisplayMetrics());
    }

    /**
     * Raise volume
     */
    @SuppressWarnings("SpellCheckingInspection")
    private void jumpstartVolume(MediaControllerCompat cn) {
        cn.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        cn.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        cn.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        cn.adjustVolume(AudioManager.ADJUST_RAISE, 0);
    }

    public void prepareAlbumsScreen() {
        FrameLayout fl = findViewById(R.id.songs_list_container);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fl.getLayoutParams();
        setMargins(fl, lp.leftMargin, dpToPx(constants.ALBUMS_LAYOUT_MARGIN), lp.rightMargin, lp.bottomMargin);
    }

    public void prepareSongsScreen() {
        FrameLayout fl = findViewById(R.id.songs_list_container);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fl.getLayoutParams();
        setMargins(fl, lp.leftMargin, dpToPx(constants.SONGS_LAYOUT_MARGIN), lp.rightMargin, lp.bottomMargin);
    }

    public void switchToSongView(Album item) {
        showPlayerButtons();                                    // Show the player buttons
        prepareSongsScreen();                                   // Change elements size for song list
        currentScreen = constants.VIEW_SONGS;
        android.app.FragmentManager fm = getFragmentManager();
        SongsFragment fragment = SongsFragment.newInstance(item.getAlbumSongs().size(), item);
        mSongsFragment = fragment;
        Bundle albumBundle = new Bundle();                      // Create Bundle to be sent to Song List Fragment
        albumBundle.putSerializable(SERIALIZE_ALBUM, item);     // Put album object in it
        fragment.setArguments(albumBundle);                     // Assign bundle to fragment
        fm.beginTransaction().replace(R.id.songs_list_container, fragment).addToBackStack(null).commit();
        getFragmentManager().executePendingTransactions();
        findViewById(R.id.songs_list_container).setVisibility(ViewGroup.VISIBLE); //Show the list again
        findViewById(R.id.songs_list_container).invalidate();                     // Tegra - I think this line can be removed

    }

    boolean albumsFragmentNotInitialized = true;

    public void switchToAlbumView() {
        currentScreen = constants.VIEW_ALBUMS;
        hidePlayerButtons();
        if (albumsFragmentNotInitialized) {
            prepareAlbumsScreen();
            currentScreen = constants.VIEW_ALBUMS;
            android.app.FragmentManager fragmentManager = getFragmentManager();
            AlbumsFragment fragment = new AlbumsFragment();
            mAlbumsFragment = fragment;
            fragmentManager.beginTransaction().replace(R.id.songs_list_container, fragment).commit();
            getFragmentManager().executePendingTransactions();
            albumsFragmentNotInitialized = false;
        } else {
            getFragmentManager().popBackStack();
            Log.i("Fragment", "popBackStack()");
        }
    }

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    //#endregion

    View.OnTouchListener clickEffect = (v, event) -> {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                v.getBackground().setColorFilter(Color.parseColor("#3304FFFF"), PorterDuff.Mode.SRC_ATOP);
                v.invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                v.getBackground().clearColorFilter();
                v.invalidate();
                break;
            }
        }
        return false;
    };


}
