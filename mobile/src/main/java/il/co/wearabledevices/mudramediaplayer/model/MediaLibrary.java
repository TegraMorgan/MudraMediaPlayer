package il.co.wearabledevices.mudramediaplayer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Tegra on 21/03/2018.
 */

public class MediaLibrary {
    private static final int ACCEPTABLE_LENGTH = 25;
    private static final String TAG = "Media Library";
    private final ArrayMap<String, Song> mMusicListById;
    private ArrayMap<String, Album> mAlbumListByName;
    private volatile State mCurrentState = State.NON_INITIALIZED;

    public MediaLibrary(Context context, String rtPath) {

        mAlbumListByName = new ArrayMap<>();
        mMusicListById = new ArrayMap<>();
        buildMediaLibrary(context, rtPath);
    }

    public MediaLibrary(Context context) {
        this(context, "/music/");
    }

    private void buildMediaLibrary(Context con, String rootPath) {
        //retrieve song info
        mCurrentState = State.INITIALIZING;
        ContentResolver resolver = con.getContentResolver();
        // TODO replace test with musicUri after finished working with virtual device
        Uri test = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //Cursor cursor = resolver.query(musicUri, null, null, null, null);
        Cursor cursor = resolver.query(test, null, null, null, null);
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
                    thisTitle = thisTitle.trim();
                    if (thisTitle.length() > ACCEPTABLE_LENGTH)
                        thisTitle = thisTitle.substring(0, ACCEPTABLE_LENGTH - 1);

                    thisArtist = cursor.getString(artistColumn);
                    if (thisArtist.compareTo("<unknown>") == 0) {
                        thisArtist = "Unknown artist";
                    }
                    thisArtist = thisArtist.trim();
                    if (thisArtist.length() > ACCEPTABLE_LENGTH)
                        thisArtist = thisArtist.substring(0, ACCEPTABLE_LENGTH - 1);


                    thisDur = (int) cursor.getLong(durationColumn) / 1000;
                    thisAlbum = cursor.getString(albumColumn);
                    if (thisAlbum == null || thisAlbum.isEmpty())
                        thisAlbum = parseDirectoryToAlbum(cursor.getString(pathColumn));
                    if (thisAlbum.compareTo("<unknown>") == 0) {
                        thisAlbum = "Unknown album";
                    }
                    thisAlbum = thisAlbum.trim();
                    if (thisAlbum.length() > ACCEPTABLE_LENGTH)
                        thisAlbum = thisAlbum.substring(0, ACCEPTABLE_LENGTH - 1);

                    thisSong = new Song(thisID, thisTitle, thisArtist, thisAlbum, thisDur);
                    Log.v(TAG, "Detected song " + thisTitle + " by " + thisArtist);
                    mMusicListById.put(String.valueOf(thisSong.getId()), thisSong);
                    addAlbumIf(mAlbumListByName, new Album(thisAlbum, thisArtist), thisSong);
                /*
                Log.v(TAG, "Song title : " + thisTitle);
                Log.v(TAG, "Artist : " + thisArtist);
                Log.v(TAG, "Album : " + thisAlbum);
                */
                }
            } while (cursor.moveToNext());
        } else {
            Log.v(TAG, "No cursor or no data in cursor!!!");
        }
        if (cursor != null) {
            //If the cursor was not null - we finished
            cursor.close();
            mCurrentState = State.INITIALIZED;
        } else {
            //If the cursor is null - something was wrong
            mCurrentState = State.NON_INITIALIZED;
        }
    }

    private void addAlbumIf(ArrayMap<String, Album> allAlbums, Album currentAlbum, Song song) {

        String can = currentAlbum.getaName();
        if (allAlbums.containsKey(can)) {
            allAlbums.get(can).getaSongs().add(song);
        } else {
            allAlbums.put(can, new Album(can, currentAlbum.getaArtist()));
            allAlbums.get(can).getaSongs().add(song);
        }
    }

    private String parseDirectoryToAlbum(String path) {
        String res;
        String[] a = path.split("/");
        res = a[a.length - 1];
        return res;
    }

    private String parseFileToSongName(String fName) {
        StringBuilder res = new StringBuilder();
        String[] a = fName.split(".");
        for (int i = 0; i < a.length - 1; i++) {
            res.append(a[i]);
        }
        return res.toString();
    }


    /**
     * @return Array of Album names as strings
     */
    public String[] getAlbumNames() {
        if (mAlbumListByName.size() == 0) return null;
        String[] result = new String[mAlbumListByName.size()];
        for (int i = 0; i < mAlbumListByName.size(); i++) {
            result[i] = mAlbumListByName.keyAt(i);
        }
        return result;
    }

    public Album getAlbum(String albumName) {
        if (mCurrentState != State.INITIALIZED || !mAlbumListByName.containsKey(albumName)) {
            return null;
        }
        return mAlbumListByName.get(albumName);
    }

    public Collection<Album> getAlbums() {
        Collection<Album> res = new ArrayList<>();
        for (Album al : mAlbumListByName.values()) {
            res.add(al);
        }

        return res;
    }

    public ArrayList<Album> getmAlbums() {
        ArrayList<Album> res = new ArrayList<>();

        return res;
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }
}
