package il.co.wearabledevices.mudramediaplayer.model;

/**
 * Created by Tegra on 12/03/2018.
 * Model for the song file
 */

public class Song {
    private long id;
    private String title;
    private String artist;
    private String album;
    private int duration;

    public Song(long songID, String songTitle, String songArtist, String songAlbum, int songDur) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        duration = songDur;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
