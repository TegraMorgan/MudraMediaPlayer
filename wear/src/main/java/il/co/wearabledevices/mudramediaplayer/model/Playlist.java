package il.co.wearabledevices.mudramediaplayer.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import il.co.wearabledevices.mudramediaplayer.constants;

@SuppressWarnings("unused")
public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    //region Variables

    private String playlistName;
    private String playlistDisplayName;
    private Bitmap albumArt;
    private ArrayList<Song> songs;

    //endregion

    //region Constructors

    public Playlist(String newPlaylistName, ArrayList<Song> songList, Bitmap albumArt) {
        this.playlistName = newPlaylistName;
        this.playlistDisplayName = newPlaylistName.substring(0, constants.ACCEPTABLE_LENGTH - 1);
        this.songs = songList;
        this.albumArt = albumArt;
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

    //endregion

    //region Helper functions

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

    public void setTrackNumbers() {
        int j = songs.size();
        int i = 0;
        sort();
        for (Song s : songs) {
            if (s.getTrackNo() == 0) s.setTrackNo(j--);
            if (s.getTrackNo() != 0) s.setTrackNo(i++);
        }
        sort();
    }

    private void sort() {
        Collections.sort(songs, (song, otherSong) -> Integer.compare(song.getTrackNo(), otherSong.getTrackNo()));
    }

    public boolean contains(String name) {
        for (Song s : songs) if (s.getTitle().equals(name)) return true;
        return false;
    }

    //endregion

    //region Getters and Setters

    public String getPlaylistName() {
        return playlistName;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public String getPlaylistDisplayName() {
        return playlistDisplayName;
    }

    //endregion
}
