package il.co.wearabledevices.mudramediaplayer;


import android.os.Bundle;

import android.support.wearable.activity.WearableActivity;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import il.co.wearabledevices.mudramediaplayer.ui.AlbumsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.SongsFragment;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.AlbumsDummyContent;
import il.co.wearabledevices.mudramediaplayer.ui.dummy.SongsDummyContent;


public class MainActivity extends WearableActivity implements AlbumsFragment.OnAlbumsListFragmentInteractionListener
        ,SongsFragment.OnSongsListFragmentInteractionListener{

    private TextView mTextView;
    static boolean isPlaying = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();

        android.app.FragmentManager fm = getFragmentManager();
        //PlayerFragment player = new PlayerFragment();

        /*SongsFragment songs = new SongsFragment();
        fm.beginTransaction().replace(R.id.songs_list_container, songs).commit();*/
        AlbumsFragment slf = new AlbumsFragment();
        fm.beginTransaction().replace(R.id.songs_list_container,slf).commit();
    }



    @Override
    public void onAlbumsListFragmentInteraction(AlbumsDummyContent.AlbumsDummyItem item) {
        android.app.FragmentManager fm = getFragmentManager();
        SongsFragment slf = new SongsFragment();

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
