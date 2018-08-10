package il.co.wearabledevices.mudramediaplayer.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.constants;

public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;
    private String pName;
    private String displayName;
    private Bitmap albumArt;
    private ArrayList<Song> songs;

    public Playlist() {

    }

    public Playlist(String nm, ArrayList<Song> sngs, Bitmap albumArt) {
        this.pName = nm;
        this.displayName = nm.substring(0, constants.ACCEPTABLE_LENGTH - 1);
        this.songs = sngs;
        this.albumArt = albumArt;
    }

    public void addSong(Song s) {
        this.songs.add(s);
    }

    public void setRandomAlbumArt() {
        int select = (int) (songs.size() * Math.random());
        albumArt = songs.get(select).getAlbumart();
    }

    public void addSongAt(Song s, int position) {
        this.songs.add(position, s);
    }

    public Playlist(Album al) {
        songs = al.getAlbumSongs();
    }

    public Playlist(ArrayList<Song> sngs) {
        songs = sngs;
    }

    public Playlist(Song sng) {
        songs = new ArrayList<>();
        songs.add(sng);
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
}
