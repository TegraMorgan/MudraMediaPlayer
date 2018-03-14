package il.co.wearabledevices.mudramediaplayer.model;

import java.util.ArrayList;

/**
 * Created by tegra on 14/03/18.
 */

public class Album {
    private String aName;
    private String aArtist;
    private ArrayList<Song> aSongs;

    public Album(String albumName, String albumArtist) {
        aName = albumName;
        aArtist = albumArtist;
        aSongs = null;
    }

    public Album(String albumName, String albumArtist, ArrayList<Song> albumSongs) {
        aName = albumName;
        aArtist = albumArtist;
        aSongs = albumSongs;
    }

    @Override
    public boolean equals(Object obj) {
        return this.aName.equals(((Album) obj).aName);
    }

    public String getaName() {
        return aName;
    }

    public void setaName(String aName) {
        this.aName = aName;
    }

    public String getaArtist() {
        return aArtist;
    }

    public void setaArtist(String aArtist) {
        this.aArtist = aArtist;
    }

    public ArrayList<Song> getaSongs() {
        return aSongs;
    }

    public void setaSongs(ArrayList<Song> aSongs) {
        this.aSongs = aSongs;
    }
}
