package il.co.wearabledevices.mudramediaplayer.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    public ArrayList<Song> songs;
    public int position;

    public Playlist() {
        songs = new ArrayList<>();
        position = 0;
    }

    public Playlist(Album al) {
        songs = al.getAlbumSongs();
        position = 0;
    }

    public Playlist(ArrayList<Song> sngs) {
        songs = sngs;
        position = 0;
    }

    public Playlist(Song sng) {
        songs = new ArrayList<>();
        songs.add(sng);
        position = 0;
    }

    public Song getCurrent() {
        if (songs.size() == 0) return null;
        return songs.get(position);
    }

    public Song skipNext() {
        if (songs.size() == 0) return null;
        if (position++ == songs.size()) position = 0;
        return songs.get(position);
    }

    public Song skipPrev() {
        if (songs.size() == 0) return null;
        if (position-- < 0) position = 0;
        return songs.get(position);
    }

    public void setPosition(int pos) {
        if (songs.size() == 0) position = 0;
        if (songs.size() >= pos) position = songs.size() - 1;
        position = pos;
    }
}
