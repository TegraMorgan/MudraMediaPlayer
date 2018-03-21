package il.co.wearabledevices.mudramediaplayer.model;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by Tegra on 21/03/2018.
 */

public class MediaLibrary {
    private ArrayList<Album> mAlbums;

    public MediaLibrary(Context context, String rtPath) {
        getSongList(context, rtPath);
    }

    public MediaLibrary(Context con) {
        getSongList(con, "/music/");
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
            do {
                if (cursor.getString(pathColumn).contains(rootPath)) {
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
}
