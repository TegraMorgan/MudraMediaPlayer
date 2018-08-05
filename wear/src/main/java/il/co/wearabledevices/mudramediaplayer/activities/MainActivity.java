package il.co.wearabledevices.mudramediaplayer.activities;


import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.wearable.android.ble.interfaces.IMudraAPI;
import com.wearable.android.ble.interfaces.IMudraDataListener;
import com.wearable.android.ble.interfaces.IMudraDeviceStatuslListener;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.constants;
import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.SongsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.utils.CustomMediaController;

import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_GESTURE;
import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_PROPORTIONAL;
import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

@SuppressWarnings("SpellCheckingInspection")
public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        , SongsFragment.OnSongsListFragmentInteractionListener, MediaController.MediaPlayerControl {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DB = "Tegra";
    private static final boolean KEEP_PLAYING_AFTER_EXIT = true;
    //#region Variables


    private MudraMusicService musicSrv;
    private CustomMediaController controller;
    private Intent playIntent;
    private boolean musicBound = false;
    private boolean paused = false, playbackPaused = true;


    private TextView mTextView;
    private AlbumsFragment mAlbumsFragment;
    private SongsFragment mSongsFragment;
    private int currentAlbumPosition = 0;
    private boolean isMudraBinded = false, mudraCallbackAdded = false, VolumeUp = false, albumsFragmentNotInitialized = true;
    private IMudraAPI mIMudraAPI = null;
    private Long lastPressureOccurrence;
    private int mudraSmoother;
    private String currentScreen;

    //#endregion

    //#region Mudra service
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
            if (mIMudraAPI != null)
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

    //#region Mudra gesture capture
    /**
     * This Listener decides what actions to take when any gesture is recognized
     */
    IMudraDataListener mMudraDataCB = new IMudraDataListener.Stub() {
        @Override
        public void onMudraDataReady(int dataType, float[] data) throws RemoteException {
            switch (dataType) {
                case DATA_TYPE_GESTURE:
                    runOnUiThread(() -> mudraGesture(data));
                    break;
                case DATA_TYPE_PROPORTIONAL:
                    runOnUiThread(() -> mudraProportional(data));
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


    //#endregion

    //#region Mudra gesture functions

    public void mudraProportional(float[] data) {
        if (data[2] > constants.MUDRA_VOLUME_PRESSURE_SENSITIVITY) {
            // Measure time from last proportional gesture
            long del = System.currentTimeMillis() - lastPressureOccurrence;
            // If there was no gesture for a long time - reset smoother
            if (del > constants.VOLUME_DIRECTION_FLIP_DELAY) {
                mudraSmoother = 0;
                VolumeUp = !VolumeUp;
                String msg = VolumeUp ? "Volume Up" : "Volume Down";
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                //Measure the proportional strength
            }
            /* Only one of three volume change commands works - change is too quick */
            if (mudraSmoother % constants.MUDRA_SMOOTH_FACTOR == 0) {
                Log.v("Tegra", "Time between pressures : " + String.valueOf(del));
                int direction = VolumeUp ? 1 : -1;
                musicSrv.adjustVolume(direction, 0);
                lastPressureOccurrence = System.currentTimeMillis();
            }
            mudraSmoother = (mudraSmoother + 1) % constants.MUDRA_SMOOTH_FACTOR;
        }
    }

    public void mudraGesture(float[] data) {
        if ((data[0] > data[1]) && (data[0] > data[2]) && (data[0] > 0.9)) {
            Log.i("INFO", "gesture: Thumb");
            // Previous song
            try {
                Log.i("Mudra interaction", "Thumb " + currentScreen);
                if (currentScreen.equals(constants.VIEW_SONGS))
                    prevSong(constants.USING_MUDRA);
                else {
                    prevAlbum();
                }

            } catch (Exception e) {
                Log.e("Tegra", e.toString());
            }
        }
        if ((data[1] > data[0]) && (data[1] > data[2]) && (data[1] > 0.9)) {
            Log.i("INFO", "gesture: Tap");
            // Play or pause
            try {
                Log.i("Mudra interaction", "Tap " + currentScreen);
                if (currentScreen.equals(constants.VIEW_SONGS))
                    play_music(constants.USING_MUDRA);
                else
                    clickAlbum();
            } catch (Exception e) {
                Log.e("Tegra", e.toString());
            }
        }
        if ((data[2] > data[0]) && (data[2] > data[1]) && (data[2] > 0.9)) {
            Log.i("INFO", "gesture: Index");
            // Next song
            try {
                Log.i("Mudra interaction", "Index " + currentScreen);
                if (currentScreen.equals(constants.VIEW_SONGS))
                    nextSong(constants.USING_MUDRA);
                else
                    nextAlbum();
            } catch (Exception e) {
                Log.e("Tegra", e.toString());
            }
        }
    }

    //#endregion

    //#region Main lifecycle functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lastPressureOccurrence = System.currentTimeMillis();
        setContentView(R.layout.activity_main);
        ImageView playPauseView = getPlayPauseView();
        findViewById(R.id.player_next).setOnTouchListener(clickEffect);
        findViewById(R.id.player_prev).setOnTouchListener(clickEffect);
        playPauseView.setOnTouchListener(clickEffect);
        mTextView = findViewById(R.id.text);
        //TODO current ambient mode is draining the battery. Make a B/W ambient screen
        setAmbientEnabled(); // Enables Always-on

        setController();

        Intent intent = new Intent();
        intent.setAction(IMudraAPI.class.getName());
        intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));
        getApplicationContext().bindService(intent, mMudraConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Starting on Resume");

        //#region Rebind mudra
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
        //#endregion

        // Check for permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //#region  We don't have permission
            Log.v(TAG, "No permission");
            // We want to request permission
            // navigate to another activity and request permission there
            Intent NavToReqPerms = new Intent(MainActivity.this, PermissionRequestActivity.class);
            startActivity(NavToReqPerms);
            //#endregion
        } else {
            // We already have permission
            //TODO Tegra Launch this on separate thread in the future

            //#region Initialize Media Library
            if (!MediaLibrary.isInitialized()) {
                ContentResolver res = getContentResolver();
                MediaLibrary.buildMediaLibrary(res);
                /*new Thread(() -> MediaLibrary.buildMediaLibrary(res));*/
            }
            //#endregion

            //#region Rebind Player Controller
            if (paused) {
                setController();
                paused = false;
            }
            if (musicBound && musicSrv.getNowPlaying() != null && musicSrv.isPlaying()) {
                playbackPaused = !musicSrv.isPlaying();
                if (musicSrv.getNowPlaying() != null && !playbackPaused) {
                    switchToSongView(musicSrv.getNowPlaying());
                    updateSongRecyclerPosition(musicSrv.getPlaylistPos());
                }
            } else {
                switchToAlbumView();
            }
            //#endregion
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(DB, "Starting");
        if (playIntent == null) {
            playIntent = new Intent(this, MudraMusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @Override
    protected void onPause() {
        getApplicationContext().unbindService(mMudraConnection);
        isMudraBinded = false;
        paused = true;
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        if (musicBound) {
            unbindService(musicConnection);
            musicBound = false;
        }
        if (!KEEP_PLAYING_AFTER_EXIT || !musicSrv.isPlaying()) {
            stopService(playIntent);
            musicSrv = null;
            /* only release mudra if the music is not playing */
            releaseMudra();
            if (isMudraBinded) {
                isMudraBinded = false;
                getApplicationContext().unbindService(mMudraConnection);
            }
            finish();
            System.exit(0);
        }
        super.onDestroy();
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
        /* --- Local variables preparation and update --- */
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }

        switchToSongView(item.mItem);
        updatePlayButton();

        /* --- Music Service controls --- */

        musicSrv.enqueueAlbum(item.mItem);
        musicSrv.playSong();


    }

    @Override
    public void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position) {
        // if back button was pressed
        if (musicSrv.getNowPlaying().getAlbumSongs().get(position).getId() == constants.BACK_BUTTON_SONG_ID) {
            switchToAlbumView();
        }
        // if regular song was selected
        else {
            Toast.makeText(this, "Playing : " + musicSrv.getNowPlaying().getAlbumSongs().get(position).getTitle(), Toast.LENGTH_LONG).show();
            musicSrv.jumpToSong(position);
            musicSrv.playSong();
            if (playbackPaused) {
                setController();
                playbackPaused = false;
            }
            //put that song in the center of the screen
            mSongsFragment.scrollToPos(position, true);
            mSongsFragment.getRecycler().getAdapter().notifyDataSetChanged();
            playbackPaused = false;
            updatePlayButton();
        }
    }

    public void play_music(View view) {
        play_music(!constants.USING_MUDRA);
    }

    public void nextSong(View view) {
        nextSong(!constants.USING_MUDRA);
    }

    public void prevSong(View view) {
        prevSong(!constants.USING_MUDRA);
    }

    /**
     * With Mudra usage only
     */
    public void nextAlbum() {
        int _albumsCount = MediaLibrary.getAlbumsCount();
        Log.i("Albums count", _albumsCount + "");
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
     * With Mudra usage only
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
     * With Mudra usage only
     */
    public void clickAlbum() {
        if (currentAlbumPosition == 0) mAlbumsFragment.getRecycler().getChildAt(0).performClick();
        else mAlbumsFragment.getRecycler().getChildAt(1).performClick();

    }

    /**
     * When using mudra, a tap on a Back view will redirect the user to the albums screen
     *
     * @param usingMudra
     */
    public void play_music(boolean usingMudra) {
        // if back button was pressed
        if (usingMudra && musicSrv.getNowPlaying().getAlbumSongs().get(musicSrv.getPlaylistPos()).getId() == constants.BACK_BUTTON_SONG_ID) {
            switchToAlbumView();
            return;
        }
        if (playbackPaused) {
            musicSrv.go();
        } else {
            musicSrv.pausePlayer();
        }
        playbackPaused = !playbackPaused;
        updatePlayButton();
        String msg = !playbackPaused ? "Playing" : "Paused";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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


    public void updatePlayButton() {
        ImageView view = getPlayPauseView();
        view.invalidate();
        view.setBackground(getDrawable(!playbackPaused ? R.drawable.pause_icon : R.drawable.play_icon));
    }

    public ImageView getPlayPauseView() {
        return findViewById(R.id.play_pause);
    }


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

    private void updateSongRecyclerPosition(int playlistPos) {
        //put the next song in the center of the screen
        mSongsFragment.getView().invalidate();
        mSongsFragment.scrollToPos(playlistPos, true);
        Log.i(TAG, "Playlist position: " + playlistPos);
        mSongsFragment.getRecycler().getAdapter().notifyDataSetChanged();
    }

    public void switchToAlbumView() {
        currentScreen = constants.VIEW_ALBUMS;
        hidePlayerButtons();
        currentAlbumPosition = 0;
        if (albumsFragmentNotInitialized) {
            prepareAlbumsScreen();
            currentScreen = constants.VIEW_ALBUMS;
            android.app.FragmentManager fragmentManager = getFragmentManager();
            AlbumsFragment fragment = new AlbumsFragment();
            //noinspection UnusedAssignment
            mAlbumsFragment = fragment;
            fragmentManager.beginTransaction().replace(R.id.songs_list_container, fragment).commit();
            getFragmentManager().executePendingTransactions();
            albumsFragmentNotInitialized = false;
        } else {
            getFragmentManager().popBackStack();
            Log.i("Fragment", "popBackStack()");
        }
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

    public void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
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

    //#endregion

    //#region Media controller and service

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MudraMusicService.MusicBinder binder = (MudraMusicService.MusicBinder) service;
            musicSrv = binder.getService();
            musicBound = true;
            // If the music is playing we want to get back to the now playing screen
            if (musicSrv != null) {
                playbackPaused = !musicSrv.isPlaying();
                if (musicSrv.getNowPlaying() != null && !playbackPaused) {
                    switchToSongView(musicSrv.getNowPlaying());
                    updateSongRecyclerPosition(musicSrv.getPlaylistPos());
                }
                musicSrv.jumpStartVolume();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    //#endregion

    //#region MediaPlayerControl implementation

    private void setController() {
        controller = new CustomMediaController(this);
        controller.setPrevNextListeners(v -> nextSong(), v -> prevSong());
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.main_screen));
        controller.setEnabled(true);
    }

    public void prevSong(boolean usingMudra) {
        if (playbackPaused) {
            setController();
        }
        if (musicSrv.getNowPlaying() == null) {
            return;
        }
        musicSrv.playPrev(usingMudra);
        playbackPaused = !isPlaying();
        updateSongRecyclerPosition(musicSrv.getPlaylistPos());
        updatePlayButton();
    }


    public void nextSong(boolean usingMudra) {
        if (playbackPaused) {
            setController();
        }
        if (musicSrv.getNowPlaying() == null) {
            return;
        }
        musicSrv.playNext(usingMudra);
        playbackPaused = !isPlaying();
        updatePlayButton();
        updateSongRecyclerPosition(musicSrv.getPlaylistPos());
    }

    public void prevSong() {
        prevSong(constants.USING_MUDRA);
    }

    public void nextSong() {
        nextSong(!constants.USING_MUDRA);
    }

    @Override
    public void start() {
        play_music(!constants.USING_MUDRA);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Tegra testing persistent service
        moveTaskToBack(true);
    }

    @Override
    public void pause() {
        play_music(!constants.USING_MUDRA);
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying()) return musicSrv.getDur();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying()) return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        return musicSrv.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
    //#endregion
}
