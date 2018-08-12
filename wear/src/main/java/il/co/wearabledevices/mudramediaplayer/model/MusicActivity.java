package il.co.wearabledevices.mudramediaplayer.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class MusicActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String aName;
    private String displayName;
    private ArrayList<Playlist> mPlaylists;
    private Bitmap activityIcon;


    public MusicActivity(String activityName, Bitmap icon) {
        aName = activityName;
        displayName = MediaLibrary.trim(activityName);
        mPlaylists = new ArrayList<>();
        activityIcon = icon;
    }

    public MusicActivity(String activityName, ArrayList<Playlist> playlists, Bitmap icon) {
        aName = activityName;
        displayName = MediaLibrary.trim(activityName);
        mPlaylists = playlists;
        activityIcon = icon;
    }

    public int getSize() {
        return mPlaylists.size();
    }

    public void addPlaylist(Playlist playlist) {
        mPlaylists.add(playlist);
    }


    public String getActivityFullName() {
        return aName;
    }

    public String getActivityDisplayName() {
        return displayName;
    }

    public ArrayList<Playlist> getActivityPlaylists() {
        return mPlaylists;
    }

    public Bitmap getActivityIcon() {
        return activityIcon;
    }
}
