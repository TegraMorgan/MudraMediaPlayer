package il.co.wearabledevices.mudramediaplayer.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class MusicActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String aName;
    private String displayName;
    private ArrayList<Playlist> mPlaylists;
    private Bitmap activityIcon;
    private int resource;

    public MusicActivity(String activityName, int resourceId) {
        aName = activityName;
        displayName = MediaLibrary.trim(activityName);
        mPlaylists = new ArrayList<>();
        resource = resourceId;
    }

    public MusicActivity(String activityName, ArrayList<Playlist> playlists, int resourceId) {
        aName = activityName;
        displayName = MediaLibrary.trim(activityName);
        mPlaylists = playlists;
        resource = resourceId;
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

    public Bitmap decodeActivityIconFromResource(Context context) {
        Bitmap albumArt = BitmapFactory.decodeResource(context.getResources(), resource);
        return albumArt;
    }
}
