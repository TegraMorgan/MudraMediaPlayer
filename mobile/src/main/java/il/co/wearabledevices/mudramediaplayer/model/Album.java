package il.co.wearabledevices.mudramediaplayer.model;

/**
 * Created by tegra on 14/03/18.
 */

public class Album {
    private String aName;
    private String aArtist;

    public Album(String albumName, String albumArtist) {
        aName = albumName;
        aArtist = albumArtist;
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
}
