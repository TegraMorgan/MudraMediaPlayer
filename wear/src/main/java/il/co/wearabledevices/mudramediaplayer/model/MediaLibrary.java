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
import java.util.concurrent.TimeUnit;

import il.co.wearabledevices.mudramediaplayer.BuildConfig;
import il.co.wearabledevices.mudramediaplayer.R;
import il.co.wearabledevices.mudramediaplayer.constants;

import static il.co.wearabledevices.mudramediaplayer.constants.BACK_BUTTON_INTERVAL;

public class MediaLibrary {
    private static final int ACCEPTABLE_LENGTH = 20;
    private static final String TAG = "Media Library";
    private static final ArrayMap<String, Song> mMusicListById = new ArrayMap<>();
    private static final ArrayMap<String, Album> mAlbumListByName = new ArrayMap<>();
    private static volatile State mCurrentState = State.NON_INITIALIZED;
    // Metadata
    public static final ArrayMap<String, MediaMetadataCompat> metadata = new ArrayMap<>();
    private static final String EMPTY_GENRE = "";
    private static final String EMPTY_ART_FILENAME = "music_metal_molder_icon";
    private static Context mContext;

    public static void buildMediaLibrary(Context con) {
        buildMediaLibrary(con, "/music/");
    }

    public static void buildMediaLibrary(Context con, String rootPath) {
        //retrieve song info
        mContext = con;
        mCurrentState = State.INITIALIZING;
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
            long thisDur;
            Song thisSong;
            String pathLowerCase;
            MediaMetadataCompat thisMetadata;
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


                    thisDur = (int) cursor.getLong(durationColumn);
                    thisAlbum = cursor.getString(albumColumn);
                    if (thisAlbum == null || thisAlbum.isEmpty())
                        thisAlbum = parseDirectoryToAlbum(cursor.getString(pathColumn));
                    if (thisAlbum.compareTo("<unknown>") == 0) {
                        thisAlbum = "Unknown album";
                    }
                    thisAlbum = thisAlbum.trim();
                    if (thisAlbum.length() > ACCEPTABLE_LENGTH)
                        thisAlbum = thisAlbum.substring(0, ACCEPTABLE_LENGTH - 1);

                    thisSong = new Song(thisID, thisTitle, thisArtist, thisAlbum, thisDur, cursor.getString(fileNameColumn), cursor.getString(pathColumn));
                    mMusicListById.put(String.valueOf(thisSong.getId()), thisSong);
                    thisMetadata = createMediaMetadataCompat(String.valueOf(thisID), thisTitle, thisArtist, thisAlbum, EMPTY_GENRE, thisDur, TimeUnit.SECONDS, EMPTY_ART_FILENAME);
                    metadata.put(String.valueOf(thisSong.getId()), thisMetadata);
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
                inflateAlbumWithBackButtons(alb);
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
    private static void inflateAlbumWithBackButtons(Album a) {
        ArrayList<Song> songs = a.getAlbumSongs();
        int songCount = a.getSongsCount();
        Song backButton = new Song(constants.BACK_BUTTON_SONG_ID, "Back", "to album selection", "to album selection2", 0, "", "");
        int backCount = songCount / BACK_BUTTON_INTERVAL;
        for (int i = backCount; i > 0; i--) {
            songs.add(i * BACK_BUTTON_INTERVAL, backButton);
        }
    }

    public static Album getAlbum(String albumName) {
        if (mCurrentState != State.INITIALIZED || !mAlbumListByName.containsKey(albumName)) {
            return null;
        }
        return mAlbumListByName.get(albumName);
    }

    public static String getSongURI(String sid) {
        return mMusicListById.get(sid).getFullPath();
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

    public static Bitmap getAlbumBitmap(Context context, String mediaId) {
        return BitmapFactory.decodeResource(context.getResources(),
                getAlbumRes(mediaId));
    }

    private static int getAlbumRes(String mediaId) {
        int res = 0;
        if (mAlbumListByName.containsKey(mediaId)) {
            Album a = mAlbumListByName.get(mediaId);
            res = a.getAlbumSongs().get(0).getAlbumRes();
        }
        return res;
    }

    public static MediaMetadataCompat getMetadata(String mediaId) {
        return getMetadata(mContext, mediaId);
    }

    public static MediaMetadataCompat getMetadata(Context context, String mediaId) {
        MediaMetadataCompat meta = mMusicListById.get(mediaId).getMetadata();
        Bitmap albumArt = getAlbumBitmap(context, mediaId);
        // Since MediaMetadataCompat is immutable, we need to create a copy to set the album art.
        // We don't set it initially on all items so that they don't take unnecessary memory.
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
        for (String key :
                new String[]{
                        MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        MediaMetadataCompat.METADATA_KEY_ALBUM,
                        MediaMetadataCompat.METADATA_KEY_ARTIST,
                        MediaMetadataCompat.METADATA_KEY_GENRE,
                        MediaMetadataCompat.METADATA_KEY_TITLE
                }) {
            builder.putString(key, meta.getString(key));
        }

        builder.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                meta.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);
        return builder.build();
    }

    public static long getMusicId(String mediaId) {
        return mMusicListById.containsKey(mediaId) ? mMusicListById.get(mediaId).getId() : null;
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

    enum State {
        NON_INITIALIZED, INITIALIZING, INITIALIZED
    }

    private static MediaMetadataCompat createMediaMetadataCompat(String mediaId, String title, String artist, String album, String genre, long duration, TimeUnit durationUnit, String albumArtResName) {
        return new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                        TimeUnit.MILLISECONDS.convert(duration, durationUnit))
                .putString(MediaMetadataCompat.METADATA_KEY_GENRE, genre)
                .putString(
                        MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                        getAlbumArtUri(albumArtResName))
                .putString(
                        MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI,
                        getAlbumArtUri(albumArtResName))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .build();
    }

    private static String getAlbumArtUri(String albumArtResName) {
        String output;
        if (albumArtResName.equals(EMPTY_ART_FILENAME)) {
            output = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + R.drawable.music_metal_molder_icon).toString();
            return output;
        } else {
            output = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + albumArtResName).toString();
            return output;
        }

        /*return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable-nodpi/" + albumArtResName;*/
    }

}
