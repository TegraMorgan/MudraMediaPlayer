package il.co.wearabledevices.mudramediaplayer.model;

import android.content.ContentResolver;
import android.support.v4.media.MediaMetadataCompat;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import il.co.wearabledevices.mudramediaplayer.BuildConfig;
import il.co.wearabledevices.mudramediaplayer.R;

/**
 * Created by Tegra on 12/03/2018.
 * Model for the song file
 */

public class Song implements Serializable {
    private static final String EMPTY_ART_FILENAME = "music_metal_molder_icon";
    private static final int EMPTY_ART_ID = R.drawable.music_metal_molder_icon;
    private static final String EMPTY_GENRE = "";
    private MediaMetadataCompat metadata;
    private long id;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private int albumRes;
    private String fileName;

    /**
     * @param songID     Resource ID
     * @param songTitle  Song Title
     * @param songArtist Artist
     * @param songAlbum  Album
     * @param songDur    Duration in miliseconds
     * @param mFlNm      File name
     */
    public Song(long songID, String songTitle, String songArtist, String songAlbum, long songDur, String mFlNm) {
        /* this constructor will be deleted in the end */
        id = songID;
        title = songTitle;
        artist = songArtist;
        album = songAlbum;
        duration = songDur;
        albumRes = EMPTY_ART_ID;
        fileName = mFlNm;
        metadata = createMediaMetadataCompat(String.valueOf(songID), songTitle, songArtist, songAlbum, EMPTY_GENRE, songDur, TimeUnit.SECONDS, EMPTY_ART_FILENAME);
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
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable/" + albumArtResName;
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

    public MediaMetadataCompat getMetadata() {
        return metadata;
    }

    public void setMetadata(MediaMetadataCompat metadata) {
        this.metadata = metadata;
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
}
