package il.co.wearabledevices.mudramediaplayer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

import il.co.wearabledevices.mudramediaplayer.model.Album;
import il.co.wearabledevices.mudramediaplayer.model.Song;

public class AlbumSelectionActivity extends AppCompatActivity {
    private static final String TAG = AlbumSelectionActivity.class.getSimpleName();
    private int numOfSongs;
    private int numOfAlbums;
    private AlbumAdapter mAdapter;
    private RecyclerView recyclerViewAlbums;
    private ArrayList<Song> mSongs;
    private ArrayList<Album> mAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_album_selection);
        Toolbar toolbar = findViewById(R.id.tb_album_selectoin);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        });

        mSongs = new ArrayList<Song>();
        mAlbums = new ArrayList<Album>();

        // get the songs
        getSongList();

        // set counters
        numOfSongs = mSongs.size();
        numOfAlbums = mAlbums.size();

        // sort the songs
        Collections.sort(mSongs, (a, b) -> a.getTitle().compareTo(b.getTitle()));
        mAlbums.sort((s, t1) -> s.getaName().compareTo(t1.getaName()));
        for (Album alb : mAlbums
                ) {
            Log.v(TAG, alb.getaName() + " : " + alb.getaArtist());

        }
        // set the recycler
        recyclerViewAlbums = findViewById(R.id.rv_albums);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAlbums.setLayoutManager(layoutManager);
        recyclerViewAlbums.setHasFixedSize(true);
        // instantiate the adapter with number of albums
        mAdapter = new AlbumAdapter(mAlbums);
        recyclerViewAlbums.setAdapter(mAdapter);

    }

    public void getSongList() {
        //retrieve song info

        ContentResolver resolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(musicUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            // count songs
            numOfSongs = cursor.getColumnIndex(MediaStore.Audio.Media._COUNT);
            //get columns
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int fileNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            long thisID;
            String thisTitle;
            String thisArtist;
            String thisAlbum;
            int thisDur;
            Song thisSong;
            do {
                thisID = cursor.getLong(idColumn);
                thisTitle = cursor.getString(titleColumn);
                if (thisTitle == null || thisTitle.isEmpty())
                    thisTitle = parseFileToSongname(cursor.getString(fileNameColumn));
                if (thisTitle.compareTo("<unknown>") == 0) {
                    thisTitle = "Unknown artist";
                }
                thisArtist = cursor.getString(artistColumn);
                if (thisArtist.compareTo("<unknown>") == 0) {
                    thisArtist = "Unknown artist";
                }
                thisDur = (int) cursor.getLong(durationColumn) / 1000;
                thisAlbum = cursor.getString(albumColumn);
                if (thisAlbum == null || thisAlbum.isEmpty())
                    thisAlbum = parseDirectoryToAlbum(cursor.getString(pathColumn));
                if (thisAlbum.compareTo("<unknown>") == 0) {
                    thisAlbum = "Unknown album";
                }

                thisSong = new Song(thisID, thisTitle, thisArtist, thisAlbum, thisDur))
                addAlbumIf(mAlbums, new Album(thisAlbum, thisArtist), thisSong);
                /*
                Log.v(TAG, "Song title : " + thisTitle);
                Log.v(TAG, "Artist : " + thisArtist);
                Log.v(TAG, "Album : " + thisAlbum);
                */
            } while (cursor.moveToNext());
        }
    }

    private void addAlbumIf(ArrayList<Album> an, Album ta, Song ts) {
        if (!an.contains(ta)) {
            ta.getaSongs().add(ts);
            an.add(ta);
        }
    }

    public String parseDirectoryToAlbum(String path) {
        String res;
        String[] a = path.split("/");
        res = a[a.length - 1];
        return res;
    }

    public String parseFileToSongname(String fname) {
        String res = "";
        String[] a = fname.split(".");
        for (int i = 0; i < a.length - 1; i++) {
            res += a[i];
        }
        return res;
    }

}
