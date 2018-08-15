package il.co.wearabledevices.mudramediaplayer.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.io.Serializable;

import il.co.wearabledevices.mudramediaplayer.R;

public class Song implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String title;
    private String artist;
    private String album;
    private String displayTitle;
    private String displayArtist;
    private String displayAlbum;
    private long duration;
    private String fileName;
    private String fullPath;
    private int trackNo;

    /**
     * @param songID     Resource ID
     * @param songTitle  Song Title
     * @param songArtist Artist
     * @param songAlbum  Album
     * @param songDur    Duration in miliseconds
     * @param mFlNm      File name
     */
    public Song(long songID, String songTitle, String songArtist, String songAlbum, int traNo, long songDur, String mFlNm, String flPth) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        displayTitle = MediaLibrary.trim(title);
        displayArtist = MediaLibrary.trim(artist);
        displayAlbum = MediaLibrary.trim(album);
        duration = songDur;
        fileName = mFlNm;
        fullPath = flPth;
        trackNo = traNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != Song.class) {
            return false;
        }

        Song that = (Song) o;

        return that.id == id;
    }

    @Override
    public int hashCode() {
        return String.valueOf(id).hashCode();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdstr() {
        return String.valueOf(id);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getDisplayArtist() {
        return displayArtist;
    }

    public String getDisplayAlbum() {
        return displayAlbum;
    }

    public int getTrackNo() {
        return trackNo;
    }

    /**
     * Extracts songs album art using song full path and provided context
     *
     * @param context Application context
     * @return A Bitmap object
     */
    public Bitmap getAlbumArt(Context context) {
        Bitmap albumArt;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, new Uri.Builder().path(this.fullPath).build());
        byte[] binaryDataAlbumArt = mmr.getEmbeddedPicture();
        if (this.id == -1) {
            albumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.baseline_arrow_back_black_18dp);
        } else if (binaryDataAlbumArt != null) {
            albumArt = BitmapFactory.decodeByteArray(binaryDataAlbumArt, 0, binaryDataAlbumArt.length);
        } else {
            albumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.music_metal_molder_icon);
        }
        return albumArt;
    }

    public void setTrackNo(int trackNo) {
        this.trackNo = trackNo;
    }
}