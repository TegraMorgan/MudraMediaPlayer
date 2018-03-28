package il.co.wearabledevices.mudramediaplayer.model;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Tegra on 12/03/2018.
 * Model for the song file
 */

public class Song implements Serializable {
    public final String trackId;
    public MediaMetadataCompat metadata;
    private long id;
    private String title;
    private String artist;
    private String album;
    private int duration;

    public Song(String trackId, MediaMetadataCompat metadata) {
        this.metadata = metadata;
        this.trackId = trackId;
    }

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int songDur) {
        /* this constructor will be deleted in the end */
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        duration = songDur;
        trackId = String.valueOf(songID);
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

        return TextUtils.equals(trackId, that.trackId);
    }

    @Override
    public int hashCode() {
        return trackId.hashCode();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public int getDuration() {return duration;}

    public void setDuration(int duration) {this.duration = duration;}

}
