package il.co.wearabledevices.mudramediaplayer.model;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.constants;

import static il.co.wearabledevices.mudramediaplayer.constants.BACK_BUTTON_INTERVAL;

public class MediaLibrary {
    private static final String TAG = "Media Library";
    private static final ArrayMap<String, Song> mMusicListById = new ArrayMap<>();
    private static final ArrayMap<String, Album> mAlbumListByName = new ArrayMap<>();
    private static final ArrayMap<String, Playlist> mPlaylists = new ArrayMap<>();

    private static volatile State mCurrentState = State.NON_INITIALIZED;

    public static void buildMediaLibrary(Resources res, ContentResolver con) {
        buildMediaLibrary(res, con, "/music/");
    }

    public static void buildMediaLibrary(Resources res, ContentResolver contentResolver, String rootPath) {
        //retrieve song info
        mCurrentState = State.INITIALIZING;
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(musicUri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            //get columns
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int fileNameColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME);
            int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            long thisID;
            String thisTitle;
            String thisArtist;
            String thisAlbum;
            long thisDur;
            Song thisSong;
            String pathLowerCase;
            Bitmap albumArt;
            byte[] data;

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

                    thisArtist = cursor.getString(artistColumn);
                    if (thisArtist.compareTo("<unknown>") == 0) {
                        thisArtist = "Unknown artist";
                    }
                    thisArtist = thisArtist.trim();


                    thisDur = (int) cursor.getLong(durationColumn);
                    thisAlbum = cursor.getString(albumColumn);
                    if (thisAlbum == null || thisAlbum.isEmpty())
                        thisAlbum = parseDirectoryToAlbum(cursor.getString(pathColumn));
                    if (thisAlbum.compareTo("<unknown>") == 0) {
                        thisAlbum = "Unknown album";
                    }
                    thisAlbum = thisAlbum.trim();

                    //region Icon extraction
                    mmr.setDataSource(cursor.getString(pathColumn));
                    data = mmr.getEmbeddedPicture();
                    if (data != null) {
                        albumArt = BitmapFactory.decodeByteArray(data, 0, data.length);
                    } else {
                        albumArt = BitmapFactory.decodeResource(res, R.drawable.music_metal_molder_icon);
                    }
                    //endregion

                    thisSong = new Song(thisID, thisTitle, thisArtist, thisAlbum, thisDur, cursor.getString(fileNameColumn), cursor.getString(pathColumn), albumArt);
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
            for (Album alb : mAlbumListByName.values()) {
                inflateAlbumWithBackButtons(res, alb);
            }
            mCurrentState = State.INITIALIZED;
        } else {
            //If the cursor is null - something was wrong
            mCurrentState = State.NON_INITIALIZED;
        }
    }

    /**
     * Receives album and adds into it back buttons
     *
     * @param a album to inflate with back buttons
     */
    private static void inflateAlbumWithBackButtons(Resources res, Album a) {
        ArrayList<Song> songs = a.getAlbumSongs();
        int songCount = a.getSongsCount();
        Song backButton = new Song(constants.BACK_BUTTON_SONG_ID, "Back", "to album selection", "to album selection2", 0, "", "", BitmapFactory.decodeResource(res, constants.BACK_BUTTON_ICON));
        int backCount = songCount / BACK_BUTTON_INTERVAL;
        for (int i = backCount; i > 0; i--) {
            songs.add(i * BACK_BUTTON_INTERVAL, backButton);
        }
        if (backCount == 0) songs.add(backButton);
    }

    public static Album getAlbum(int index) {
        if (mCurrentState != State.INITIALIZED || mAlbumListByName.size() < index + 1) {
            return null;
        }
        return mAlbumListByName.valueAt(index);
    }


    public static Collection<Album> getAlbums() {
        Collection<Album> res = new ArrayList<>();
        for (Album al : mAlbumListByName.values()) {
            res.add(al);
        }
        return res;
    }

    public static ArrayList<Album> getmAlbums() {
        return (ArrayList<Album>) getAlbums();
    }

    public static int getAlbumsCount() {
        return mAlbumListByName.size();
    }

    private static void addAlbumIf(ArrayMap<String, Album> allAlbums, Album currentAlbum, Song song) {

        String can = currentAlbum.getAlbumName();
        if (allAlbums.containsKey(can)) {
            allAlbums.get(can).getAlbumSongs().add(song);
        } else {
            allAlbums.put(can, new Album(can, currentAlbum.getaArtist()));
            allAlbums.get(can).getAlbumSongs().add(song);
        }
    }

    private static String parseDirectoryToAlbum(String path) {
        String res;
        String[] a = path.split("/");
        res = a[a.length - 1];
        return res;
    }

    private static String parseFileToSongName(String fName) {
        StringBuilder res = new StringBuilder();
        String[] a = fName.split(".");
        for (int i = 0; i < a.length - 1; i++) {
            res.append(a[i]);
        }
        return res.toString();
    }

    public static boolean isInitialized() {
        return mCurrentState == State.INITIALIZED;
    }

    public static String trim(String s) {
        return s.substring(0, constants.ACCEPTABLE_LENGTH - 1);
    }

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

}
