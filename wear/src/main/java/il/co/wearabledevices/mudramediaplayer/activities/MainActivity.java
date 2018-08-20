package il.co.wearabledevices.mudramediaplayer.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.model.MusicActivity;
import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.model.Song;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.MusicActivityAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.MusicActivityFragment;
import il.co.wearabledevices.mudramediaplayer.ui.PlayListAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.PlayListFragment;
import il.co.wearabledevices.mudramediaplayer.ui.PlayerFragment;
import il.co.wearabledevices.mudramediaplayer.ui.SongsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume;
import il.co.wearabledevices.mudramediaplayer.utils.CustomMediaController;

import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_GESTURE;
import static il.co.wearabledevices.mudramediaplayer.constants.DATA_TYPE_PROPORTIONAL;
import static il.co.wearabledevices.mudramediaplayer.constants.VIEW_ACTIVITIES;
import static il.co.wearabledevices.mudramediaplayer.constants.VIEW_BACKGROUND;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.IDLE;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.P1;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.P2;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.P3;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.PM1;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.PM2;
import static il.co.wearabledevices.mudramediaplayer.utils.AnnotationVolume.PM3;

@SuppressWarnings("SpellCheckingInspection")
public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        , SongsFragment.OnSongsListFragmentInteractionListener, MediaController.MediaPlayerControl,
        MusicActivityFragment.OnMusicActivityFragmentInteractionListener,
        PlayListFragment.OnPlayListFragmentInteractionListener,
        PlayerFragment.OnFragmentInteractionListener,
        MediaPlayer.OnCompletionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String DB = "Tegra";
    private static final boolean KEEP_PLAYING_AFTER_EXIT = false;
    //#region Variables


    private static MudraMusicService musicSrv;
    private CustomMediaController controller;
    private Intent playIntent;
    private static boolean musicBound = false;
    private static boolean playbackPaused = true;

    private static FragmentManager mFragmentManager;
    private static AnnotationVolume volState;
    private static SongsFragment mSongsFragment;
    private static PlayListFragment mPlaylistsFragment;
    private static MusicActivityFragment mMusicActivitiesFragment;
    private View currentUpperView = null;

    private static int currentDepth = 0;

    /**
     * This variable saves the position of a selector at a given menu depth
     */
    private static int[] cursorAtDepth = new int[3];
    private static int[] currentPathPlaying = new int[3];
    private static boolean backButtonSelected = false;
    private static boolean isMudraBinded = false, mudraCallbackAdded = false;
    private IMudraAPI mIMudraAPI = null;
    private static Long lastPressureOccurrence;
    private static String currentScreen;
    private static Handler mHandler;


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
            Log.e(TAG, ex.toString());
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
            Log.e(TAG, ex.toString());
        }
    }

    public void startSNCDataTransmission() {
        try {
            mIMudraAPI.startRawSNCDataTransmission();
        } catch (RemoteException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public void stopSNCDataTransmission() {
        try {
            mIMudraAPI.stopRawSNCDataTransmission();
        } catch (RemoteException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public void disonnectMudra() {
        try {
            mIMudraAPI.disconnectMudraDevice();
        } catch (RemoteException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public void releaseMudra() {
        try {
            if (mIMudraAPI != null)
                mIMudraAPI.releaseMudra();
        } catch (RemoteException ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private void bindMudra() {
        Intent intent = new Intent();
        intent.setAction(IMudraAPI.class.getName());
        intent.setComponent(new ComponentName("com.wearable.android.ble", "com.wearable.android.ble.service.BluetoothLeService"));
        getApplicationContext().bindService(intent, mMudraConnection, Context.BIND_AUTO_CREATE);
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
                Log.e(TAG, ex.toString());
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
        float a = data[2];
        Log.v("Mudra proportional", "Volume state : " + String.valueOf(volState.state));
        lastPressureOccurrence = System.currentTimeMillis();
        if (volState.state == IDLE && a > constants.MUDRA_VOLUME_PRESSURE_SENSITIVITY) {
            volState.state = P1;
            mHandler.postDelayed(volumePoll, constants.mInterval);
        }
        switch (volState.state) {
            case P1:
                if (a > 0.6) volState.state = P2;
                else if (a < 0.4) volState.state = PM1;
                break;
            case P2:
                if (a > 0.7) volState.state = P3;
                else if (a < 0.6) volState.state = P1;
                break;
            case P3:
                if (a < 0.6) volState.state = P2;
                break;
            case PM1:
                if (a > 0.5) volState.state = P1;
                else if (a < 0.3) volState.state = PM2;
                break;
            case PM2:
                if (a < 0.2) volState.state = PM3;
                else if (a > 0.4) volState.state = PM1;
                break;
            case PM3:
                if (a < 0.1) {
                    volState.state = IDLE;
                    Log.v("Mudra proportional", "volume state reset");
                } else if (a > 0.3) volState.state = PM2;
                break;
            default:
                break;
        }
    }

    public void mudraGesture(float[] data) {
        //region Thumb action
        if ((data[0] > data[1]) && (data[0] > data[2]) && (data[0] > 0.9)) {
            try {
                if (currentScreen.equals(constants.VIEW_SONGS))
                    scrollSongBack(constants.USING_MUDRA);
                else {
                    if (currentScreen.equals(constants.VIEW_PLAYLISTS)) {
                        scrollPlaylistsBack(constants.USING_MUDRA);
                    } else {
                        if (currentScreen.equals(constants.VIEW_ACTIVITIES)) {
                            scrollActivitiesBack(constants.USING_MUDRA);
                        } else {
                            if (currentScreen.equals(constants.VIEW_NOW_PLAYING)) {
                                navigateToPreviousScreen();
                            } else {
                                if (currentScreen.equals(constants.VIEW_BACKGROUND)) {
                                    prevSong(constants.USING_MUDRA);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        //endregion

        //region Tap action
        if ((data[1] > data[0]) && (data[1] > data[2]) && (data[1] > 0.9)) {
            try {
                if (backButtonSelected) {
                    // Try to go back one screen
                    backButtonSelected = false;
                    dimGeneralBackButton();
                    cursorAtDepth[currentDepth] = cursorAtDepth[currentDepth] < 0 ? 0 : cursorAtDepth[currentDepth];
                    navigateToPreviousScreen();
                } else {
                    if (currentScreen.equals(constants.VIEW_SONGS)) {
                        tapOnSongInPlaylist();
                    } else {
                        if (currentScreen.equals(constants.VIEW_PLAYLISTS)) {
                            // first we get in what activity we are
                            // then we get the playlist that was selected and pass it to tapOnPlaylist
                            tapOnPlaylist(MediaLibrary.getMusicActivities().valueAt(cursorAtDepth[0]).getActivityPlaylists().get(cursorAtDepth[1]));
                        } else {
                            if (currentScreen.equals(constants.VIEW_ACTIVITIES)) {
                                tapOnActivity(MediaLibrary.getMusicActivities().valueAt(cursorAtDepth[0]));
                            } else {
                                if (currentScreen.equals(constants.VIEW_NOW_PLAYING)) {
                                    //play or pause music
                                    MainActivityPlayMusic(true);
                                } else {
                                    if (currentScreen.equals(constants.VIEW_BACKGROUND)) {
                                        generalPlayPause();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        //endregion

        //region Index action
        if ((data[2] > data[0]) && (data[2] > data[1]) && (data[2] > 0.9)) {
            try {
                if (currentScreen.equals(constants.VIEW_SONGS))
                    scrollSongForward(constants.USING_MUDRA);
                else {
                    if (currentScreen.equals(constants.VIEW_PLAYLISTS)) {
                        scrollPlaylistsForward(constants.USING_MUDRA);
                    } else {
                        if (currentScreen.equals(constants.VIEW_ACTIVITIES)) {
                            scrollActivitiesForward(constants.USING_MUDRA);
                        } else {
                            if (currentScreen.equals(constants.VIEW_NOW_PLAYING)) {
                                nextSong(true);
                            } else {
                                if (currentScreen.equals(VIEW_BACKGROUND)) {
                                    nextSong(true);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        //endregion

    }

    //#endregion

    //#region Main lifecycle functions
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        lastPressureOccurrence = System.currentTimeMillis();
        setContentView(R.layout.activity_main);
        //TODO current ambient mode is draining the battery. Make a B/W ambient screen
        setAmbientEnabled(); // Enables Always-on
        setController();
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                MediaLibrary.buildMediaLibrary(this.getResources(), res);
                /*new Thread(() -> MediaLibrary.buildMediaLibrary(res));*/
            }
            //#endregion

            // if we get back from sound change
            Log.d(TAG, "onResume: current screen is - " + currentScreen);
            if (currentScreen != null && !currentScreen.equals(VIEW_BACKGROUND) && !currentScreen.equals(VIEW_ACTIVITIES)) {
                restoreCurrentPath();
                dimGeneralBackButton();
                while (mFragmentManager.popBackStackImmediate()) ;
                Log.d(TAG, "onResume: fragment stack is empty : " + !mFragmentManager.popBackStackImmediate());
                String t = currentScreen;
                switchToActivityView();
                if (t.equals(constants.VIEW_PLAYLISTS) || t.equals(constants.VIEW_SONGS) || t.equals(constants.VIEW_NOW_PLAYING))
                    switchToPlaylistsView(MediaLibrary.getMusicActivities().valueAt(cursorAtDepth[0]));
                if (t.equals(constants.VIEW_SONGS) || t.equals(constants.VIEW_NOW_PLAYING))
                    switchToSongListView(MediaLibrary.getMusicActivities().valueAt(cursorAtDepth[0]).getActivityPlaylists().get(cursorAtDepth[1]));
                if (t.equals(constants.VIEW_NOW_PLAYING))
                    showNowPlaying(musicSrv.getCurrentSong());
                Log.d(TAG, "onResume: got to the end");
            } else {
                //#region Rebind mudra

                if (!isMudraBinded) {
                    bindMudra();
                } else {
                    getApplicationContext().unbindService(mMudraConnection);
                    bindMudra();
                }
                isMudraBinded = true;
                //#endregion
                Log.d(TAG, "onResume: Volume state machine : " + volState);
                Log.d(TAG, "onResume: music service : " + musicSrv);
                if (volState == null) {
                    Log.d(TAG, "onResume: reset navigation");
                    prepareVolumeStateMachine();
                    prepareNavigationVariables();
                }

                switchToActivityView();
            }
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
        Log.d(TAG, "onPause: fired");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: fired");
        if (musicBound) {
            musicSrv.beforeMainActivityUnbind();
            unbindService(musicConnection);
            musicBound = false;
        }
        releaseMudra();
        getApplicationContext().unbindService(mMudraConnection);
        isMudraBinded = false;
        if (!KEEP_PLAYING_AFTER_EXIT || !musicSrv.isPlaying()) {
            stopService(playIntent);
            musicSrv = null;
            finish();
            System.exit(0);
        }
        currentScreen = constants.VIEW_BACKGROUND;
        currentDepth = -1;
        backButtonSelected = false;
        for (int i = 0; i < 3; i++) cursorAtDepth[i] = cursorAtDepth[i] < 0 ? 0 : cursorAtDepth[i];
        super.onDestroy();
        System.gc();
    }

    private void prepareVolumeStateMachine() {
        volState = new AnnotationVolume(IDLE);
        mHandler = new Handler();
        startRepeatingTask();
    }

    void startRepeatingTask() {
        volumePoll.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(volumePoll);
    }

    Runnable volumePoll = new Runnable() {
        @Override
        public void run() {
            try {
                int curr;
                curr = musicSrv.getCurrentVolume();
                if (System.currentTimeMillis() - lastPressureOccurrence > constants.mInterval) {
                    volState.state = IDLE;
                    Log.v("volumePoll", "volume state reset");
                }
                Log.v("volumePoll", "Volume state : " + String.valueOf(volState.state));
                if (volState.state != IDLE) {
                    Log.v("volumePoll", "New volume is : " + String.valueOf(curr + volState.state));
                    musicSrv.setVolume(curr + volState.state);
                }
            } catch (Exception e) {
                if (e.getClass() != NullPointerException.class) throw e;
            } finally {
                if (volState.state != IDLE)
                    mHandler.postDelayed(volumePoll, constants.mInterval);
            }
        }
    };

    private void prepareNavigationVariables() {
        for (int i = 0; i < 3; i++) cursorAtDepth[i] = 0;
        currentDepth = 0;
    }

    //#endregion

    //#region Fragment interaction functions

    /**
     * Fires when user selects album to play
     *
     * @param item Album the user has selected
     * @deprecated this method will be deleted eventually
     */
    @Override
    public void onAlbumsListFragmentInteraction(AlbumsAdapter.ViewHolder item, int position) {
        // This will be deleted in the end Tegra
    }

    @Override
    public void onMusicActivityFragmentInteraction(MusicActivityAdapter.ViewHolder item, int position) {
        cursorAtDepth[0] = position;
        tapOnActivity(item.mItem);
    }

    @Override
    public void onPlayListFragmentInteraction(PlayListAdapter.ViewHolder item, int position) {
        cursorAtDepth[1] = position;
        tapOnPlaylist(item.mItem);
    }

    @Override
    public void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position) {
        Toast.makeText(this, "Playing : " + item.mItem.getDisplayTitle(), Toast.LENGTH_SHORT).show();
        cursorAtDepth[2] = position;
        tapOnSongInPlaylist();
    }

    private void showNowPlaying(Song song) {
        Log.d(TAG, "showNowPlaying: song - " + song.getTitle());
        Log.d(TAG, "showNowPlaying: cursor - " + cursorAtDepth[2]);
        findViewById(R.id.songs_list_container).setVisibility(View.INVISIBLE);
        PlayerFragment pf = new PlayerFragment();
        Bundle bdl = new Bundle();
        bdl.putString(constants.SONG_ARTIST, song.getDisplayTitle());
        bdl.putString(constants.SONG_TITLE, song.getDisplayArtist());
        pf.setArguments(bdl);
        mFragmentManager.beginTransaction().replace(R.id.upper_container, pf).addToBackStack(null).commit();
        findViewById(R.id.top_fragment_text).setVisibility(View.INVISIBLE);
        saveCurrentPath();
        currentScreen = constants.VIEW_NOW_PLAYING;
        currentDepth = 3;
        updateMainActivityBackgroundWithSongAlbumArt();
    }

    public void showGeneralBackButton() {
        findViewById(R.id.general_back_button).setVisibility(View.VISIBLE);
    }

    public void hideGeneralBackButton() {
        findViewById(R.id.general_back_button).setVisibility(View.INVISIBLE);
    }

    public void MainActivityPlayMusic(View view) {
        MainActivityPlayMusic(!constants.USING_MUDRA);
    }

    /**
     * @UNUSED Don't remove it yet
     */
    public void play_music2(View view) {
        MainActivityPlayMusic(!constants.USING_MUDRA);
    }

    public void nextSong(View view) {
        nextSong(!constants.USING_MUDRA);
    }

    /**
     * @deprecated With Mudra usage only
     */
    public void nextAlbum() {
/*
        int _albumsCount = MediaLibrary.getAlbumsCount();
        Log.i("Albums count", _albumsCount + "");
        if (cursorAtDepth[0] < _albumsCount - 1) {
            cursorAtDepth[0] += 1;
            //put the next song in the center of the screen
            mAlbumsFragment.scrollToPos(cursorAtDepth[0], true);
            Log.i("current position", cursorAtDepth[0] + "");
            mAlbumsFragment.getRecycler().getAdapter().notifyDataSetChanged();
        } else {
            Log.i("current position", "reached the end");
        }
*/
    }

    /**
     * @deprecated With Mudra usage only
     */
    public void prevAlbum() {

/*
        if (cursorAtDepth[0] > 0) {
            cursorAtDepth[0] -= 1;
            //put the next song in the center of the screen
            mAlbumsFragment.scrollToPos(cursorAtDepth[0], true);
            Log.i("current position", cursorAtDepth[0] + "");
            mAlbumsFragment.getRecycler().getAdapter().notifyDataSetChanged();
        } else {
            Log.i("current position", "reached the beginning");
        }
*/
    }

    /**
     * When using mudra, a tap on a Back view will redirect the user to the albums screen
     *
     * @param usingMudra
     */
    public void MainActivityPlayMusic(boolean usingMudra) {
        generalPlayPause();
        updatePlayButton();
        String msg = !playbackPaused ? "Playing" : "Paused";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void generalPlayPause() {
        if (playbackPaused) {
            musicSrv.startPlayer();
            updateMainActivityBackgroundWithSongAlbumArt();
        } else {
            musicSrv.pausePlayer();
        }
        playbackPaused = !playbackPaused;
    }

    /**
     * @deprecated
     */
    public void showPlayerButtons() {
        //region delete
        /*Hide the list for now - for better paging (not the best thing yet)*/
        /*findViewById(R.id.songs_list_container).setVisibility(View.INVISIBLE);
        findViewById(R.id.songs_list_container).invalidate();

        TextView albums_text = findViewById(R.id.player_albums_text);
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        albums_text.setVisibility(View.INVISIBLE);
        albums_text.invalidate();
        player_prev.setVisibility(View.VISIBLE);
        player_next.setVisibility(View.VISIBLE);
        player_play.setVisibility(View.VISIBLE);*/
//endregion

        /* ------------------------------------------- */
        FrameLayout mainLayout = findViewById(R.id.upper_container);
        mainLayout.removeAllViews();
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        currentUpperView = inflater.inflate(R.layout.top_fragment_player, mainLayout, true);

    }

    public void updatePlayButton() {
        ImageView view = getPlayPauseView();
        view.invalidate();
        view.setBackground(getDrawable(!playbackPaused ? R.drawable.uamp_ic_pause_white_48dp : R.drawable.uamp_ic_play_arrow_white_48dp));
        view.invalidate();
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

    private void updateSongRecyclerPosition(int playlistPos) {
        //put the next song in the center of the screen
        try {
            mSongsFragment.getView().invalidate();
            mSongsFragment.scrollToPos(playlistPos, true);
            mSongsFragment.getRecycler().getAdapter().notifyDataSetChanged();
        } catch (NullPointerException e) {
            Log.e(TAG, "updateSongRecyclerPosition:", e);
        }
    }

    private void updatePlaylistRecyclerPosition(int actPos) {
        //put the next Playlist in the center of the screen
        try {
            mPlaylistsFragment.getView().invalidate();
            mPlaylistsFragment.scrollToPos(actPos, true);
            Log.i(TAG, "Playlist position: " + actPos);
            mPlaylistsFragment.getRecycler().getAdapter().notifyDataSetChanged();
        } catch (NullPointerException e) {
            Log.e(TAG, "updateSongRecyclerPosition:", e);
        }
    }

    private void updateMusicActivitiesRecyclerPosition(int pos) {
        //put next activitie in the center of the screen
        try {
            mMusicActivitiesFragment.getView().invalidate();
            mMusicActivitiesFragment.scrollToPos(pos, true);
            Log.i(TAG, "Playlist position: " + pos);
            mMusicActivitiesFragment.getRecycler().getAdapter().notifyDataSetChanged();
        } catch (NullPointerException e) {
            Log.e(TAG, "updateSongRecyclerPosition:", e);
        }
    }

    /**
     * @deprecated use switchToActivityView() or switchToPlaylistsView() instead, and later delete this method
     */
    public void switchToAlbumView() {
        currentScreen = constants.VIEW_ALBUMS;
        showTopLabelAndBackButton();
        /*cursorAtDepth[0] = 0;
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
        }*/
        /************** ACTIVITY_FRAGMENT ***************/
        MusicActivityFragment maf = new MusicActivityFragment();
        mFragmentManager.beginTransaction().replace(R.id.songs_list_container, maf).commit();
    }

    public void switchToActivityView() {
        Log.d(TAG, "switchToActivityView: ");
        currentScreen = constants.VIEW_ACTIVITIES;
        currentDepth = 0;
        showTopLabelAndBackButton();
        hideGeneralBackButton();
        mMusicActivitiesFragment = new MusicActivityFragment();
        mFragmentManager.beginTransaction().replace(R.id.songs_list_container, mMusicActivitiesFragment).commit();
        findViewById(R.id.songs_list_container).setVisibility(ViewGroup.VISIBLE);
    }

    public void switchToPlaylistsView(MusicActivity a) {
        Log.d(TAG, "switchToPlaylistsView: activity - " + a.getActivityDisplayName());
        Log.d(TAG, "switchToPlaylistsView: cursor was : " + cursorAtDepth[0]);
        currentScreen = constants.VIEW_PLAYLISTS;
        currentDepth = 1;
        showTopLabelAndBackButton();
        showGeneralBackButton();
        this.<TextView>findViewById(R.id.top_fragment_text).setText(constants.LABEL_PLAYLISTS);
        mPlaylistsFragment = new PlayListFragment();
        Bundle bdl = new Bundle();
        bdl.putSerializable(constants.MUSIC_ACTIVITY, a);
        mPlaylistsFragment.setArguments(bdl);
        mFragmentManager.beginTransaction().replace(R.id.songs_list_container, mPlaylistsFragment).addToBackStack(null).commit();
    }

    public void switchToSongListView(Playlist item) {
        Log.d(TAG, "switchToSongListView: songlist - " + item.getPlaylistName());
        Log.d(TAG, "switchToSongListView: cursor was : " + cursorAtDepth[1]);
        currentScreen = constants.VIEW_SONGS;
        currentDepth = 2;
        prepareSongListView();
        Bundle bdl = new Bundle();
        bdl.putSerializable(constants.PLAY_LIST, item);
        SongsFragment sf = new SongsFragment();
        mSongsFragment = sf;
        sf.setArguments(bdl);
        mFragmentManager.beginTransaction().replace(R.id.songs_list_container, sf).addToBackStack(null).commit();
        mFragmentManager.executePendingTransactions();
        if (musicSrv.isPlaying())
            updateMainActivityBackgroundWithSongAlbumArt();
    }

    public void showTopLabelAndBackButton() {
        FrameLayout mainLayout = findViewById(R.id.upper_container);
        mainLayout.removeAllViews();
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        currentUpperView = inflater.inflate(R.layout.top_fragment_albums, mainLayout, true);

    }

    private void prepareSongListView() {
        showTopLabelAndBackButton();
        findViewById(R.id.songs_list_container).setVisibility(ViewGroup.VISIBLE);
        this.<TextView>findViewById(R.id.top_fragment_text).setText(constants.LABEL_SONGS);
        findViewById(R.id.top_fragment_text).setVisibility(View.VISIBLE);
        showGeneralBackButton();
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

    /**
     * Changes main activity background to current song's album art
     */
    private void updateMainActivityBackgroundWithSongAlbumArt() {
        findViewById(R.id.main_background).setBackground(new BitmapDrawable(getResources(), musicSrv.getCurrentSong().getAlbumArt(getApplicationContext())));
    }

    /**
     * Sets background to supplied Bitmap
     *
     * @param bit bitmap to be set as a background
     */
    private void setMainActivityBackground(Bitmap bit) {
        findViewById(R.id.main_background).setBackground(new BitmapDrawable(getResources(), bit));
    }

    private void highlightGeneralBackButton() {
        // Hightlight the back button #D0FF00
        View e = findViewById(R.id.general_back_button);
        BitmapDrawable c = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.left_filled));
        e.setBackground(c);
        e.getBackground().setTint(0xFFD0FF00);
        // Dim song list
        findViewById(R.id.songs_list_container).setAlpha(0.3f);
    }

    private void dimGeneralBackButton() {
        // Return back button to normal #E0E0E0
        View e = findViewById(R.id.general_back_button);
        BitmapDrawable c = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.baseline_arrow_back_black_18dp));
        e.setBackground(c);
        e.getBackground().setTint(Color.parseColor("#E0E0E0"));
        // Return song list to normal
        findViewById(R.id.songs_list_container).setAlpha(1f);
    }

    private void tapOnSongInPlaylist() {
        currentDepth = 2;
        // play song
        Log.d(TAG, "tapOnSongInPlaylist: cursor is : " + cursorAtDepth[currentDepth]);
        musicSrv.setNowPlayingPosition(cursorAtDepth[currentDepth]);
        musicSrv.playSong();
        if (playbackPaused) setController();
        // set the player variables
        playbackPaused = false;
        updateMainActivityBackgroundWithSongAlbumArt();
        //show player screen
        showNowPlaying(musicSrv.getCurrentSong());
    }

    private void tapOnPlaylist(Playlist pl) {
        switchToSongListView(pl);
        musicSrv.enqueuePlaylist(pl);
        musicSrv.playSong();
        playbackPaused = false;
        cursorAtDepth[2] = musicSrv.getPlaylistPos();
        showNowPlaying(musicSrv.getCurrentSong());
    }

    private void tapOnActivity(MusicActivity ac) {
        cursorAtDepth[1] = 0;
        switchToPlaylistsView(ac);
        setMainActivityBackground(ac.decodeActivityIconFromResource(this));
        showGeneralBackButton();
    }

    private void saveCurrentPath() {
        System.arraycopy(cursorAtDepth, 0, currentPathPlaying, 0, 3);
    }

    private void restoreCurrentPath() {
        Log.d(TAG, "restoreCurrentPath: path restored");
        System.arraycopy(currentPathPlaying, 0, cursorAtDepth, 0, 3);
    }


    //#endregion

    //#region Media controller and service

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MudraMusicService.MusicBinder binder = (MudraMusicService.MusicBinder) service;
            musicSrv = binder.getService();
            binder.setCallback(MainActivity.this);
            musicBound = true;
            // If the music is playing we want to get back to the now playing screen
            if (musicSrv != null) {
                if (musicSrv.isPlaying() && musicSrv.getNowPlaying() != null) {
                    // We will follow the saved path from currentPathPlaying
                    playbackPaused = !musicSrv.isPlaying();
                    restoreCurrentPath();
                    while (mFragmentManager.popBackStackImmediate()) ;
                    Log.d(TAG, "onServiceConnected: fragment stack is empty : " + !mFragmentManager.popBackStackImmediate());
                    String t = currentScreen;
                    switchToActivityView();
                    if (t.equals(constants.VIEW_PLAYLISTS) || t.equals(constants.VIEW_SONGS) || t.equals(constants.VIEW_NOW_PLAYING))
                        switchToPlaylistsView(MediaLibrary.getMusicActivities().valueAt(cursorAtDepth[0]));
                    if (t.equals(constants.VIEW_SONGS) || t.equals(constants.VIEW_NOW_PLAYING))
                        switchToSongListView(MediaLibrary.getMusicActivities().valueAt(cursorAtDepth[0]).getActivityPlaylists().get(cursorAtDepth[1]));
                    if (t.equals(constants.VIEW_NOW_PLAYING))
                        showNowPlaying(musicSrv.getCurrentSong());
                    Log.d(TAG, "onServiceConnected: got to the end");
                } else {
                    musicSrv.jumpStartVolume();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Update graphics here
        Log.d(TAG, "onCompletion: MainActivity");
        musicSrv.onCompletion(mp);
        updateMainActivityBackgroundWithSongAlbumArt();
        if (currentScreen.equals(constants.VIEW_NOW_PLAYING)) {
            updateNowPlayingArtistAndSongName();
        }
        cursorAtDepth[2] = musicSrv.getPlaylistPos();
        currentPathPlaying[2] = cursorAtDepth[2];
    }

    private void updateNowPlayingArtistAndSongName() {
        TextView artist, song;
        artist = findViewById(R.id.song_artist);
        song = findViewById(R.id.song_title);
        artist.setText(musicSrv.getCurrentSong().getDisplayArtist());
        artist.setSelected(true);
        song.setText(musicSrv.getCurrentSong().getDisplayTitle());
        song.setSelected(true);

    }


    //#endregion

    //#region MediaPlayerControl implementation

    private void setController() {
        controller = new CustomMediaController(this);
        controller.setPrevNextListeners(v -> nextSong(), v -> prevSong());
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.main_screen));
        controller.setEnabled(true);
    }

    public void scrollSongBack(boolean usingMudra) {
        // if there is a song to go back
        --cursorAtDepth[2];
        if (cursorAtDepth[2] >= 0) {
            updateSongRecyclerPosition(cursorAtDepth[2]);
            currentPathPlaying[currentDepth] = cursorAtDepth[currentDepth];
        } else {
            if(!backButtonSelected){
                Toast.makeText(this,"Tap to go up",Toast.LENGTH_LONG).show();
            }
            backButtonSelected = true;
            // if there is no song - transition to back button highlight
            highlightGeneralBackButton();
        }
    }

    public void scrollPlaylistsBack(boolean usingMudra) {
        // if there is a playlist to go back
        if (--cursorAtDepth[1] >= 0) {
            updatePlaylistRecyclerPosition(cursorAtDepth[1]);
        } else {
            if(!backButtonSelected){
                Toast.makeText(this,"Tap to go up",Toast.LENGTH_LONG).show();
            }
            backButtonSelected = true;
            highlightGeneralBackButton();
        }
    }

    public void scrollActivitiesBack(boolean usingMudra) {
        updateMusicActivitiesRecyclerPosition(cursorAtDepth[0] > 0 ? --cursorAtDepth[0] : 0);
    }

    public void scrollSongForward(boolean usingMudra) {
        if (cursorAtDepth[2] < 0) {
            // If we are at the back button
            // reset cursor to first song
            cursorAtDepth[2] = 0;
            // bring song list back and "grey" the back button
            dimGeneralBackButton();
            backButtonSelected = false;
        } else {
            cursorAtDepth[2] = (cursorAtDepth[2] + 1) % mSongsFragment.getRecycler().getAdapter().getItemCount();
            currentPathPlaying[currentDepth] = cursorAtDepth[currentDepth];
            updateSongRecyclerPosition(cursorAtDepth[2]);
        }
    }

    public void scrollPlaylistsForward(boolean usingMudra) {
        if (cursorAtDepth[1] < 0) {
            cursorAtDepth[1] = 0;
            dimGeneralBackButton();
            backButtonSelected = false;
        } else {
            cursorAtDepth[1] = (cursorAtDepth[1] + 1) % mPlaylistsFragment.getRecycler().getAdapter().getItemCount();
            updatePlaylistRecyclerPosition(cursorAtDepth[1]);
        }
    }

    public void scrollActivitiesForward(boolean usingMudra) {
        cursorAtDepth[0] = (cursorAtDepth[0] + 1) % mMusicActivitiesFragment.getRecycler().getAdapter().getItemCount();
        updateMusicActivitiesRecyclerPosition(cursorAtDepth[0]);
    }

    public void nextSong(boolean usingMudra) {
        if (playbackPaused) {
            setController();
        }
        if (musicSrv.getNowPlaying() == null) {
            return;
        }
        musicSrv.playNext(usingMudra);
        cursorAtDepth[2] = musicSrv.getPlaylistPos();
        currentPathPlaying[2] = cursorAtDepth[2];
        playbackPaused = false;
        if (!currentScreen.equals(VIEW_BACKGROUND)) {
            updateMainActivityBackgroundWithSongAlbumArt();
            updateNowPlayingArtistAndSongName();
            updatePlayButton();
        }
    }

    public void prevSong() {
        prevSong(false);
    }

    public void prevSong(boolean usingMudra) {
        if (playbackPaused) {
            setController();
        }
        if (musicSrv.getNowPlaying() == null) {
            return;
        }
        musicSrv.playPrev(usingMudra);
        cursorAtDepth[2] = musicSrv.getPlaylistPos();
        currentPathPlaying[2] = cursorAtDepth[2];
        playbackPaused = false;
        if (!currentScreen.equals(VIEW_BACKGROUND)) {
            updateMainActivityBackgroundWithSongAlbumArt();
            updateNowPlayingArtistAndSongName();
        }
    }

    public void nextSong() {
        scrollSongForward(!constants.USING_MUDRA);
    }

    @Override
    public void start() {
        MainActivityPlayMusic(!constants.USING_MUDRA);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Tegra testing persistent service
        moveTaskToBack(true);
    }

    @Override
    public void pause() {
        MainActivityPlayMusic(!constants.USING_MUDRA);
    }

    @Override
    public int getDuration() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying()) return musicSrv.getDuration();
        else
            return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPlaying())
            return musicSrv.getPositionInSong();
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(this, "Player", Toast.LENGTH_SHORT).show();
    }

    public void navigateToPreviousScreen(View view) {
        navigateToPreviousScreen();
    }

    public void navigateToPreviousScreen() {
        Log.d(TAG, "navigateToPreviousScreen: ");
        String prevScreen = getPreviousScreen();
        Log.d(TAG, "navigateToPreviousScreen: " + prevScreen);
        if (prevScreen != null) {
            if (mFragmentManager.popBackStackImmediate()) {
                ((TextView) findViewById(R.id.top_fragment_text)).setText(getPreviousLabel());
                currentScreen = prevScreen;
                currentDepth--;
                Log.d(TAG, "navigateToPreviousScreen: depth is " + currentDepth);
                if (currentScreen.equals(constants.VIEW_ACTIVITIES)) {
                    hideGeneralBackButton();
                    mMusicActivitiesFragment.scrollToPos(cursorAtDepth[0], true);
                } else {
                    if (currentScreen.equals(constants.VIEW_PLAYLISTS)) {
                        mPlaylistsFragment.scrollToPos(cursorAtDepth[1], true);
                    } else {
                        if (currentScreen.equals(constants.VIEW_SONGS)) {
                            prepareSongListView();
                            mSongsFragment.scrollToPos(cursorAtDepth[2], true);
                        }
                    }
                }
            } else {
                switchToActivityView();
            }
        }
    }

    public String getPreviousScreen() {
        switch (currentScreen) {
            case constants.VIEW_NOW_PLAYING:
                currentDepth = 3;
                return constants.VIEW_SONGS;
            case constants.VIEW_SONGS:
                currentDepth = 2;
                return constants.VIEW_PLAYLISTS;
            case constants.VIEW_PLAYLISTS:
                currentDepth = 1;
                return constants.VIEW_ACTIVITIES;
            case constants.VIEW_ACTIVITIES:
                currentDepth = 0;
                return constants.VIEW_ACTIVITIES;
            default:
                return null;
        }
    }

    public String getPreviousLabel() {
        switch (currentScreen) {
            case constants.VIEW_SONGS:
                return constants.LABEL_PLAYLISTS;
            case constants.VIEW_PLAYLISTS:
                return constants.LABEL_ACTIVITIES;
            default:
                return null;
        }
    }
    //#endregion
}
