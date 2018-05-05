package il.co.wearabledevices.mudramediaplayer;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.model.Song;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.SongsDummyContent;

import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        ,SongsFragment.OnSongsListFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mTextView;
    static boolean isPlaying = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        /* Tegra - check permission and prepare media library */
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
            MediaLibrary.buildMediaLibrary(this);
        }
        Collection albumsOnDevice = MediaLibrary.getAlbums();
        /* End Tegra */

        android.app.FragmentManager fm = getFragmentManager();
        //PlayerFragment player = new PlayerFragment();

        /*SongsFragment songs = new SongsFragment();
        fm.beginTransaction().replace(R.id.songs_list_container, songs).commit();*/
        AlbumsFragment slf = new AlbumsFragment();
        fm.beginTransaction().replace(R.id.songs_list_container,slf).commit();
    }


    @Override
    public void onAlbumsListFragmentInteraction(Album item) {
        android.app.FragmentManager fm = getFragmentManager();
        SongsFragment slf = new SongsFragment();
        // Create Bundle to be sent to Song List Fragment
        Bundle bdl = new Bundle();
        // put album object in it
        bdl.putSerializable(SERIALIZE_ALBUM, item);
        slf.setArguments(bdl);
        fm.beginTransaction().replace(R.id.songs_list_container,slf).addToBackStack(null).commit();
    }

    @Override
    public void onSongsListFragmentInteraction(Song item) {
        Toast.makeText(this,item.getFileName(),Toast.LENGTH_LONG).show();

    }

    public void play_music(View view) {
        view.setBackground(getDrawable(isPlaying? R.drawable.pause_icon:R.drawable.play_icon));
        isPlaying = !isPlaying;
    }
}
