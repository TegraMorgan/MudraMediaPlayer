package il.co.wearabledevices.mudramediaplayer.model;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("unused")
public class Playlist implements Serializable {
    private static final long serialVersionUID = 1L;

    //region Variables

    private String playlistName;
    private String playlistDisplayName;
    private int albArt;
    private ArrayList<Song> songs;

    //endregion

    //region Constructors

    public Playlist(String nm, ArrayList<Song> songList) {
        setPlaylistName(nm);
        this.songs = songList;
    }

    public Playlist(Album album) {
        songs = album.getSongs();
        setPlaylistName(album.getAlbumName());
    }

    public Playlist(Song song, String nm) {
        songs = new ArrayList<>();
        songs.add(song);
        setPlaylistName(nm);
    }

    private void setPlaylistName(String nm) {
        this.playlistName = nm;
        this.playlistDisplayName = MediaLibrary.trim(this.playlistName);
    }

    //endregion

    //region Helper functions

    public void addSong(Song s) {
        this.songs.add(s);
    }

    public void setRandomAlbumArt() {
        albArt = (int) (songs.size() * Math.random());
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

    public Bitmap getAlbumArt(Context con) {
        return songs.get(albArt).getAlbumArt(con);
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public String getPlaylistDisplayName() {
        return playlistDisplayName;
    }

    public int getSongsCount() {
        return songs.size();
    }

    //endregion
}
