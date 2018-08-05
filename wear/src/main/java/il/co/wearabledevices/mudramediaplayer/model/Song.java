package il.co.wearabledevices.mudramediaplayer.model;

import android.graphics.Bitmap;

import java.io.Serializable;

import il.co.wearabledevices.mudramediaplayer.R;

public class Song implements Serializable {
    private static final int EMPTY_ART_ID = R.drawable.music_metal_molder_icon;
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private int albumRes;
    private String fileName;
    private String fullPath;
    private Bitmap albumart;

    /**
     * @param songID     Resource ID
     * @param songTitle  Song Title
     * @param songArtist Artist
     * @param songAlbum  Album
     * @param songDur    Duration in miliseconds
     * @param mFlNm      File name
     */
    public Song(long songID, String songTitle, String songArtist, String songAlbum, long songDur, String mFlNm, String flPth, Bitmap alba) {
        /* this constructor will be deleted in the end */
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        duration = songDur;
        albumRes = EMPTY_ART_ID;
        fileName = mFlNm;
        fullPath = flPth;
        albumart = alba;
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


    public int getAlbumRes() {
        return albumRes;
    }

    public void setAlbumRes(int albumRes) {
        this.albumRes = albumRes;
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

    public Bitmap getAlbumart() {
        return albumart;
    }
}
