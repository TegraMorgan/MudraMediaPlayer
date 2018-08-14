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
    private static final ArrayMap<String, Album> mAlbumListByName = new ArrayMap<>();
    private static final ArrayMap<String, Playlist> mPlaylists = new ArrayMap<>();
    private static final ArrayMap<String, MusicActivity> mActivities = new ArrayMap<>();

    private static String mCurrentState = "NON_INITIALIZED";

    //region Build Media Library functions

    public static void buildMediaLibrary(Resources res, ContentResolver con) {
        buildMediaLibrary(res, con, "/music/");
    }

    public static void buildMediaLibrary(Resources res, ContentResolver contentResolver, String rootPath) {
        //retrieve song info
        mCurrentState = "INITIALIZING";
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
            int trackNoColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK);

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            long thisID;
            String thisTitle;
            String thisArtist;
            String thisAlbum;
            String thisPlaylistName;
            long thisDur;
            Song thisSong;
            int thisTrackNo;
            String pathLowerCase;
            Bitmap albumArt;
            byte[] binaryDataAlbumArt;

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
                    thisPlaylistName = parseDirectoryToAlbum(cursor.getString(pathColumn)).trim();
                    thisTrackNo = cursor.getInt(trackNoColumn);

                    //region Icon extraction
                    mmr.setDataSource(cursor.getString(pathColumn));
                    binaryDataAlbumArt = mmr.getEmbeddedPicture();
                    if (binaryDataAlbumArt != null) {
                        albumArt = BitmapFactory.decodeByteArray(binaryDataAlbumArt, 0, binaryDataAlbumArt.length);
                    } else {
                        albumArt = BitmapFactory.decodeResource(res, R.drawable.music_metal_molder_icon);
                    }
                    //endregion

                    // Create song object
                    thisSong = new Song(thisID, thisTitle, thisArtist, thisAlbum, thisTrackNo, thisDur, cursor.getString(fileNameColumn), cursor.getString(pathColumn), albumArt);
                    // Add it to album
                    addAlbumIf(mAlbumListByName, new Album(thisAlbum, thisArtist), thisSong);
                    // Add it to Playlist
                    addPlaylistIf(mPlaylists, thisSong, thisPlaylistName);
                }
            } while (cursor.moveToNext());
        } else {
            Log.v(TAG, "No cursor or no data in cursor");
        }
        if (cursor != null) {
            //If the cursor was not null - we finished
            cursor.close();
            for (Album alb : mAlbumListByName.values()) {
                inflateAlbumWithBackButtons(res, alb);
            }
            for (String key : mPlaylists.keySet()) {
                mPlaylists.get(key).setRandomAlbumArt();
                mPlaylists.get(key).setTrackNumbers();
            }
            PrepareMusicActivities(res);
            mCurrentState = "INITIALIZED";
        } else {
            //If the cursor is null - something was wrong
            mCurrentState = "NON_INITIALIZED";
        }
    }

    private static void PrepareMusicActivities(Resources res) {
        /* For now MusicActivities are hand made */
        MusicActivity act = new MusicActivity("Run", BitmapFactory.decodeResource(res, R.drawable.running));
        act.addPlaylist(mPlaylists.get("Walk On The Beach"));
        act.addPlaylist(mPlaylists.get("Motivation Mix"));
        act.addPlaylist(mPlaylists.get("Zumba Beats"));
        act.addPlaylist(mPlaylists.get("Give it all"));
        mActivities.put(act.getActivityFullName(), act);
        act = new MusicActivity("Gym", BitmapFactory.decodeResource(res, R.drawable.gym));
        act.addPlaylist(mPlaylists.get("Beast Mode"));
        act.addPlaylist(mPlaylists.get("Hype"));
        act.addPlaylist(mPlaylists.get("Power Workout"));
        act.addPlaylist(mPlaylists.get("Give it all"));
        mActivities.put(act.getActivityFullName(), act);
        act = new MusicActivity("Biking", BitmapFactory.decodeResource(res, R.drawable.biking));
        act.addPlaylist(mPlaylists.get("Beast Mode"));
        act.addPlaylist(mPlaylists.get("Motivation Mix"));
        act.addPlaylist(mPlaylists.get("Give it all"));
        mActivities.put(act.getActivityFullName(), act);
    }

    private static void addPlaylistIf(ArrayMap<String, Playlist> pl, Song s, String nm) {
        Playlist a = pl.get(nm);
        if (a != null) a.addSong(s);
        else pl.put(nm, new Playlist(s));
    }

    /**
     * Receives album and adds into it back buttons
     *
     * @param a album to inflate with back buttons
     */
    private static void inflateAlbumWithBackButtons(Resources res, Album a) {
        ArrayList<Song> songs = a.getAlbumSongs();
        int songCount = a.getSongsCount();
        Song backButton = new Song(constants.BACK_BUTTON_SONG_ID, "Back", "to album selection", "to album selection2", 0, 0, "", "", BitmapFactory.decodeResource(res, constants.BACK_BUTTON_ICON));
        int backCount = songCount / BACK_BUTTON_INTERVAL;
        for (int i = backCount; i > 0; i--) {
            songs.add(i * BACK_BUTTON_INTERVAL, backButton);
        }
        if (backCount == 0) songs.add(backButton);
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

    public static String trim(String s) {
        if (s.length() <= constants.ACCEPTABLE_LENGTH) return s;
        return s.substring(0, constants.ACCEPTABLE_LENGTH - 1);
    }

    //endregion

    //region Getters

    public static Album getAlbum(int index) {
        if (!mCurrentState.equals("INITIALIZED") || mAlbumListByName.size() < index + 1) {
            return null;
        }
        return mAlbumListByName.valueAt(index);
    }

    public static Collection<Album> getAlbums() {
        return new ArrayList<>(mAlbumListByName.values());
    }

    public static int getAlbumsCount() {
        return mAlbumListByName.size();
    }

    public static boolean isInitialized() {
        return mCurrentState.equals("INITIALIZED");
    }

    public static ArrayMap<String, Playlist> getPlaylists() {
        return mPlaylists;
    }

    public static ArrayMap<String, MusicActivity> getMusicActivities() {
        return mActivities;
    }

    //endregion
}
