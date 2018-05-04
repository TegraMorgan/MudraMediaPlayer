package il.co.wearabledevices.mudramediaplayer;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.SongsDummyContent;

import static il.co.wearabledevices.mudramediaplayer.constants.SERIALIZE_ALBUM;

public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        ,SongsFragment.OnSongsListFragmentInteractionListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_MEDIA_ACCESS = 4769;
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
            // We still don't have permission
            Log.v(TAG, "No permission");
            // We request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_MEDIA_ACCESS);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MEDIA_ACCESS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    //TODO Tegra Launch this on separate thread in the future
                    MediaLibrary.buildMediaLibrary(this);
                } else {
                    //permission denied
                    // Basel?
                }
            }
        }
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
    public void onSongsListFragmentInteraction(SongsDummyContent.SongsDummyItem item) {
        Toast.makeText(this,item.content,Toast.LENGTH_LONG).show();

    }

    public void play_music(View view) {
        view.setBackground(getDrawable(isPlaying? R.drawable.pause_icon:R.drawable.play_icon));
        isPlaying = !isPlaying;
    }
}
