package il.co.wearabledevices.mudramediaplayer.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.constants;

@SuppressWarnings("unused")
public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    private String pName;
    private String displayName;
    private Bitmap albumArt;
    private ArrayList<Song> songs;

    public Playlist(String newPlaylistName, ArrayList<Song> songList, Bitmap albumArt) {
        this.pName = newPlaylistName;
        this.displayName = newPlaylistName.substring(0, constants.ACCEPTABLE_LENGTH - 1);
        this.songs = songList;
        this.albumArt = albumArt;
    }

    public void addSong(Song s) {
        this.songs.add(s);
    }

    public void setRandomAlbumArt() {
        int select = (int) (songs.size() * Math.random());
        albumArt = songs.get(select).getAlbumArt();
    }

    public void addSongAt(Song s, int position) {
        this.songs.add(position, s);
    }

    public Playlist(Album album) {
        songs = album.getAlbumSongs();
    }

    public Playlist(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public Playlist(Song song) {
        songs = new ArrayList<>();
        songs.add(song);
    }

    public String getpName() {
        return pName;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void sort() {
        ArrayList<Song> h = new ArrayList<Song>(songs.size());
        for (Song s : songs) {
            h.add(s.getTrackNo(), s);
        }
        songs = h;
    }
}
