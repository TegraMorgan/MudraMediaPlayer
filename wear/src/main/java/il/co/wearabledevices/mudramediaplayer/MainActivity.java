package il.co.wearabledevices.mudramediaplayer;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.model.Song;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;

import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        , SongsFragment.OnSongsListFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    static boolean isPlaying = false;
    TextView albums_text;
    private TextView mTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);
        albums_text = findViewById(R.id.player_albums);
        // Enables Always-on
        setAmbientEnabled();

        /* Tegra - Permissions and fragment initialization were moved to onResume */


        //PlayerFragment player = new PlayerFragment();
        /*SongsFragment songs = new SongsFragment();
        fm.beginTransaction().replace(R.id.songs_list_container, songs).commit();*/
    }


    @Override
    protected void onResume() {
        super.onResume();
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
            android.app.FragmentManager fm = getFragmentManager();
            AlbumsFragment slf = new AlbumsFragment();
            fm.beginTransaction().replace(R.id.songs_list_container, slf).commit();
        }
    }

    @Override
    public void onAlbumsListFragmentInteraction(Album item) {
        //Show the player buttons upon album selection
        showPlayerButtons();

        android.app.FragmentManager fm = getFragmentManager();
        SongsFragment slf = new SongsFragment();
        // Create Bundle to be sent to Song List Fragment
        Bundle bdl = new Bundle();
        // put album object in it
        bdl.putSerializable(SERIALIZE_ALBUM, item);
        slf.setArguments(bdl);
        fm.beginTransaction().replace(R.id.songs_list_container, slf).addToBackStack(null).commit();
    }

    @Override
    public void onSongsListFragmentInteraction(Song item) {
        Toast.makeText(this, item.getFileName(), Toast.LENGTH_LONG).show();

    }

    public void play_music(View view) {
//        view.setBackground(getDrawable(isPlaying? R.drawable.play_icon:R.drawable.pause_icon));
//        isPlaying = !isPlaying;

        showAlbumsScreen();

    }

    public void showPlayerButtons() {
        GridLayout gl = findViewById(R.id.above);
        gl.removeView(albums_text);
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.VISIBLE);
        player_next.setVisibility(View.VISIBLE);
        player_play.setVisibility(View.VISIBLE);
    }

    public void showAlbumsScreen() {
        GridLayout gl = findViewById(R.id.above);
        gl.addView(albums_text);
        ImageView player_prev = findViewById(R.id.player_prev);
        ImageView player_play = findViewById(R.id.play_pause);
        ImageView player_next = findViewById(R.id.player_next);
        player_prev.setVisibility(View.INVISIBLE);
        player_next.setVisibility(View.INVISIBLE);
        player_play.setVisibility(View.INVISIBLE);

    }

    @Override
    public void onBackPressed() {
        SongsFragment test = (SongsFragment) getFragmentManager().findFragmentByTag(SongsFragment.class.getSimpleName());
        if (test != null && test.isVisible()) {
            //show Albums page header
            showAlbumsScreen();
        }
        super.onBackPressed();
    }
}
