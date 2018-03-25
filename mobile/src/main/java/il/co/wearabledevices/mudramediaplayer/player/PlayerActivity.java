package il.co.wearabledevices.mudramediaplayer.player;

/**
 * Created by Baselscs on 25/03/2018.
 */


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.model.Album;


public class PlayerActivity extends AppCompatActivity
        implements PlayerFragment.OnFragmentInteractionListener
        ,SongsFragment.OnFragmentInteractionListener,SongsListFragment.OnListFragmentInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        FragmentManager fm = getSupportFragmentManager();
        PlayerFragment player = new PlayerFragment();
        fm.beginTransaction().replace(R.id.player_container,player).commit();

        /*SongsFragment songs = new SongsFragment();
        fm.beginTransaction().replace(R.id.songs_list_container, songs).commit();*/
        SongsListFragment slf = new SongsListFragment();
        fm.beginTransaction().replace(R.id.songs_list_container,slf).commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Toast.makeText(this, "yep", Toast.LENGTH_SHORT).show();

    }

    public void test(View view) {
        Toast.makeText(this, "Yep",
                Toast.LENGTH_LONG).show();
    }



    @Override
    public void onListFragmentInteraction(Album item) {
        Toast.makeText(this, item.toString(),
                Toast.LENGTH_LONG).show();
    }
}
