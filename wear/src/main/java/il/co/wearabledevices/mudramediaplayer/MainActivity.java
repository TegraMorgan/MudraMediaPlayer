package il.co.wearabledevices.mudramediaplayer;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.MediaBrowserProvider;
import il.co.wearabledevices.mudramediaplayer.ui.SongsAdapter;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.utils.LogHelper;

import static il.co.wearabledevices.mudramediaplayer.constants.ENQUEUE_ALBUM;
import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        , SongsFragment.OnSongsListFragmentInteractionListener, MediaBrowserProvider {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int ALBUMS_LAYOUT_MARGIN = 0;
    private static final int SONGS_LAYOUT_MARGIN = 74;
    static boolean isPlaying = false;
    ImageView playPauseView;
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
    TextView albums_text;
    private TextView mTextView;
    private MediaBrowserCompat mMediaBrowser;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playPauseView = findViewById(R.id.play_pause);
        mTextView = findViewById(R.id.text);
        //albums_text = findViewById(R.id.player_albums);
        // Enables Always-on
        setAmbientEnabled();

        // Connect a media browser just to get the media session token. There are other ways
        // this can be done, for example by sharing the session token directly.
        mMediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MusicService.class), mConnectionCallback, null);

        /* Tegra - Permissions and fragment initialization were moved to onResume */
    }

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

    @Override
    public MediaBrowserCompat getMediaBrowser() {
        return mMediaBrowser;
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
        mediaController.registerCallback(mMediaControllerCallback);
        onMediaControllerConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Starting on Resume");
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

    /* MUDRA CONTENT */

    @Override
    public void onAlbumsListFragmentInteraction(Album item) {
        //Show the player buttons upon album selection
        //showPlayerButtons();
        showPlayerButtons();
        showSongsScreen();
        play_music(null);
        android.app.FragmentManager fm = getFragmentManager();
        SongsFragment slf = SongsFragment.newInstance(item.getaSongs().size(), item);
        // Create Bundle to be sent to Song List Fragment
        Bundle bdl = new Bundle();
        // put album object in it
        bdl.putSerializable(SERIALIZE_ALBUM, item);
        slf.setArguments(bdl);
        fm.beginTransaction().replace(R.id.songs_list_container, slf).addToBackStack(null).commit();


        // Enqueue all the album and play it
        Bundle bndl = new Bundle();
        bndl.putSerializable(SERIALIZE_ALBUM, item);
        MediaControllerCompat.TransportControls mediaController = MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls();
        mediaController.sendCustomAction(ENQUEUE_ALBUM, bndl);

    }

    @Override
    public void onSongsListFragmentInteraction(SongsAdapter.SongsViewHolder item, int position) {

        Toast.makeText(this, String.valueOf(position), Toast.LENGTH_LONG).show();
        MediaControllerCompat.getMediaController(MainActivity.this).getTransportControls()
                .skipToQueueItem(position);


    }

    public void play_music(View view) {
        isPlaying = !isPlaying;
        if (view == null)
            view = playPauseView;
        view.setBackground(getDrawable(isPlaying ? R.drawable.pause_icon : R.drawable.play_icon));

        //showAlbumsScreen();
    }

    public void showPlayerButtons() {
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.VISIBLE);
        player_next.setVisibility(View.VISIBLE);
        player_play.setVisibility(View.VISIBLE);
    }

    public void hidePlayerButtons(){
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.INVISIBLE);
        player_next.setVisibility(View.INVISIBLE);
        player_play.setVisibility(View.INVISIBLE);
    }

    public int dpToPx(int sizeInDP){
        int marginInDp = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, sizeInDP, getResources()
                        .getDisplayMetrics());
        return marginInDp;
    }
    public void showAlbumsScreen() {
        FrameLayout fl = findViewById(R.id.songs_list_container);
        FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)fl.getLayoutParams();

        setMargins(fl,lp.leftMargin,dpToPx(ALBUMS_LAYOUT_MARGIN),lp.rightMargin,lp.bottomMargin);
    }

    public void showSongsScreen() {
        FrameLayout fl = findViewById(R.id.songs_list_container);
        FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)fl.getLayoutParams();

        setMargins(fl,lp.leftMargin,dpToPx(SONGS_LAYOUT_MARGIN),lp.rightMargin,lp.bottomMargin);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }
//    @Override
//    public void onBackPressed() {
//        SongsFragment test = (SongsFragment) getFragmentManager().findFragmentByTag(SongsFragment.class.getSimpleName());
//        if (test != null && test.isVisible()) {
//            //show Albums page header
//            showAlbumsScreen();
//        }
//        super.onBackPressed();
//    }

    protected void onMediaControllerConnected() {

        //getBrowseFragment().onConnected();
    }
}
