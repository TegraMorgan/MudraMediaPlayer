package il.co.wearabledevices.mudramediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.service.MudraMusicService;
import il.co.wearabledevices.mudramediaplayer.service.MudraMusicService.MusicBinder;

public class AlbumSelectionActivity extends AppCompatActivity implements AlbumAdapter.ListItemClickListener {
    private static final String TAG = AlbumSelectionActivity.class.getSimpleName();
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerViewAlbums;
    private MediaLibrary mLibrary;
    private MudraMusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound;
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicBinder binder = (MusicBinder) service;
            // get the service pointer
            musicSrv = binder.getService();
            //mark as bounded
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MudraMusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }

    @SuppressWarnings("Convert2Diamond")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_selection);
        Toolbar toolbar = findViewById(R.id.tb_album_selectoin);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            //TODO floating button action
            stopService(playIntent);
            musicSrv = null;
            System.exit(0);
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        });

        /* This is an example how to use MediaLibrary class */

        /* mLibrary - Global variable of MediaLibrary class */
        /* Initialize new media library with default root path (music) */
        mLibrary = new MediaLibrary(this);
        /* get the albums from the media library */
        ArrayList<Album> mAlbums = mLibrary.getmAlbums();
        /* sort the albums by album name */
        mAlbums.sort(Comparator.comparing(Album::getaName));

        /* end of MediaLibrary example */

        // set the recycler
        recyclerViewAlbums = findViewById(R.id.rv_albums);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAlbums.setLayoutManager(layoutManager);
        recyclerViewAlbums.setHasFixedSize(true);
        // instantiate the adapter with number of albums
        mAdapter = new AlbumAdapter(mAlbums, this);
        recyclerViewAlbums.setAdapter(mAdapter);

    }


    @Override
    protected void onDestroy() {
        Log.v(TAG, "OnDestroy launched");
        stopService(playIntent);
        unbindService(musicConnection);
        musicSrv = null;

        super.onDestroy();
    }

    @Override
    public void onListItemClick(int cii) {
        Album sel = mLibrary.getmAlbums().get(cii);
        musicSrv.setList(new Playlist(sel));
        musicSrv.playSong();
    }
}
