package il.co.wearabledevices.mudramediaplayer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Tegra on 21/03/2018.
 */

public class MediaLibrary {
    private static final String TAG = "Media Library";
    private final ConcurrentMap<String, Song> mMusicListById;
    private ConcurrentMap<String, List<MediaMetadataCompat>> mAlbums;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public MediaLibrary(Context context, String rtPath) {

        mAlbums = new ConcurrentHashMap<>();
        mMusicListById = new ConcurrentHashMap<>();
        getSongList(context, rtPath);
    }

    public MediaLibrary(Context context) {
        this(context, "/music/");
    }

    public Iterable<String> getAlbums() {
        if (mCurrentState != State.INITIALIZED) {
            return Collections.emptyList();
        }
        return mAlbums.keySet();
    }

    public List<MediaMetadataCompat> getMusicsByAlbum(String albumName) {
        if (mCurrentState != State.INITIALIZED || !mAlbums.containsKey(albumName)) {
            return Collections.emptyList();
        }
        return mAlbums.get(albumName);
    }

    public void getSongList(Context con, String rootPath) {
        //retrieve song info

        ContentResolver resolver = con.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(musicUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
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
            String pathLowerCase;
            do {
                pathLowerCase = cursor.getString(pathColumn).toLowerCase();
                if (pathLowerCase.contains(rootPath)) {
                    thisID = cursor.getLong(idColumn);
                    thisTitle = cursor.getString(titleColumn);
                    if (thisTitle == null || thisTitle.isEmpty())
                        thisTitle = parseFileToSongName(cursor.getString(fileNameColumn));
                    if (thisTitle.compareTo("<unknown>") == 0) {
                        thisTitle = "Unknown song";
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

                    thisSong = new Song(thisID, thisTitle, thisArtist, thisAlbum, thisDur);
                    addAlbumIf(mAlbums, new Album(thisAlbum, thisArtist), thisSong);
                /*
                Log.v(TAG, "Song title : " + thisTitle);
                Log.v(TAG, "Artist : " + thisArtist);
                Log.v(TAG, "Album : " + thisAlbum);
                */
                }
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void addAlbumIf(ArrayList<Album> an, Album ta, Song ts) {
        int ind = an.indexOf(ta);
        if (ind == -1) {
            ta.getaSongs().add(ts);
            an.add(ta);
        } else {
            an.get(ind).getaSongs().add(ts);
        }
    }

    public String parseDirectoryToAlbum(String path) {
        String res;
        String[] a = path.split("/");
        res = a[a.length - 1];
        return res;
    }

    public String parseFileToSongName(String fName) {
        StringBuilder res = new StringBuilder();
        String[] a = fName.split(".");
        for (int i = 0; i < a.length - 1; i++) {
            res.append(a[i]);
        }
        return res.toString();
    }

    private synchronized void buildListsByAlbum() {
        ConcurrentMap<String, List<MediaMetadataCompat>> newMusicListByGenre = new ConcurrentHashMap<>();

        for (Song m : mMusicListById.values()) {
            String genre = m.metadata.getString(MediaMetadataCompat.METADATA_KEY_GENRE);
            List<MediaMetadataCompat> list = newMusicListByGenre.get(genre);
            if (list == null) {
                list = new ArrayList<>();
                newMusicListByGenre.put(genre, list);
            }
            list.add(m.metadata);
        }
        mAlbums = newMusicListByGenre;
    }

    /**
     * @return Array of Album names as strings
     */
    public String[] getAlbumNames() {
        if (mAlbums.size() == 0) return null;
        String[] result = new String[mAlbums.size()];
        for (int i = 0; i < mAlbums.size(); i++) {
            result[i] = mAlbums.get(i).getaName();
        }
        return result;
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }
}
