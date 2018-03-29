package il.co.wearabledevices.mudramediaplayer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.util.ArrayMap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import il.co.wearabledevices.mudramediaplayer.R;

/**
 * Created by Tegra on 21/03/2018.
 */

public class MediaLibrary {
    private static final int ACCEPTABLE_LENGTH = 25;
    private static final String TAG = "Media Library";
    private static final ArrayMap<String, Song> mMusicListById = new ArrayMap<>();
    private static final ArrayMap<String, Album> mAlbumListByName = new ArrayMap<>();
    private static volatile State mCurrentState = State.NON_INITIALIZED;

    public static void buildMediaLibrary(Context con, String rootPath) {
        //retrieve song info
        mCurrentState = State.INITIALIZING;
        ContentResolver resolver = con.getContentResolver();
        // TODO replace test with musicUri after finished working with virtual device
        Uri test = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //Cursor cursor = resolver.query(musicUri, null, null, null, null);
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

    /**
     * @return Array of Album names as strings
     */
    public static String[] getAlbumNames() {
        if (mAlbumListByName.size() == 0) return null;
        String[] result = new String[mAlbumListByName.size()];
        for (int i = 0; i < mAlbumListByName.size(); i++) {
            result[i] = mAlbumListByName.keyAt(i);
        }
        return result;
    }

    public static Album getAlbum(String albumName) {
        if (mCurrentState != State.INITIALIZED || !mAlbumListByName.containsKey(albumName)) {
            return null;
        }
        return mAlbumListByName.get(albumName);
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

    public static Bitmap getAlbumBitmap(Context context, String string) {
        /*
        return BitmapFactory.decodeResource(context.getResources(),
                MusicLibrary.getAlbumRes(mediaId));
         */
        // TODO load album image instead
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.android_music_player_rand);
    }

    public static MediaMetadataCompat getMetadata(Context context, String mediaId) {
        Song song = mMusicListById.get(mediaId);
        Bitmap albumArt = getAlbumBitmap(context, mediaId);
        // Since MediaMetadataCompat is immutable, we need to create a copy to set the album art.
        // We don't set it initially on all items so that they don't take unnecessary memory.
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, song.getIdstr());
        builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.getAlbum());
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.getArtist());
        //builder.putString(MediaMetadataCompat.METADATA_KEY_GENRE,song.getGenre()); //For future use
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.getTitle());
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.getDuration());
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        return builder.build();
    }

    public static long getMusicId(String mediaId) {
        return mMusicListById.containsKey(mediaId) ? mMusicListById.get(mediaId).getId() : null;
    }

    private static void addAlbumIf(ArrayMap<String, Album> allAlbums, Album currentAlbum, Song song) {

        String can = currentAlbum.getaName();
        if (allAlbums.containsKey(can)) {
            allAlbums.get(can).getaSongs().add(song);
        } else {
            allAlbums.put(can, new Album(can, currentAlbum.getaArtist()));
            allAlbums.get(can).getaSongs().add(song);
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

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

}
