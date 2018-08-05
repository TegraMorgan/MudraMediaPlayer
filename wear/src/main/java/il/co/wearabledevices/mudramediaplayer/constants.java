package il.co.wearabledevices.mudramediaplayer;

import android.media.AudioManager;

public class constants {
    public static final int DATA_TYPE_GESTURE = 0;
    public static final int DATA_TYPE_PROPORTIONAL = 1;
    public static final int DATA_TYPE_SNC = 2;
    public static final int DATA_TYPE_QUATERNIONS = 3;
    public static final int DATA_TYPE_ACCNORM = 4;
    public static final int DATA_TYPE_UIACTION = 5;

    public static final String SERIALIZE_ALBUM = "serialize_album";
    public static final int REQUEST_MEDIA_ACCESS = 4769;
    public static final String ENQUEUE_ALBUM = "play_album";

    public static final int BACK = 0;
    public static final int SELECT = 1;
    public static final int SCROLL_DN_RIGHT = 2;
    public static final int SCROLL_UP_LEFT = 3;

    public static final String CURRENT_SONG_RECYCLER_POSITION = "CURRENT_POSITION";
    public static final String CURRENT_ALBUM_SIZE = "CURRENT_ALBUM_SIZE";
    public static final int ALBUMS_LAYOUT_MARGIN = 60;
    public static final int SONGS_LAYOUT_MARGIN = 74;
    public static final int BACK_BUTTON_SONG_ID = -2;
    public static final int BACK_BUTTON_INTERVAL = 3;
    // 0<x<1 Higher values will demand more pressure to be applied
    public static final double MUDRA_VOLUME_PRESSURE_SENSITIVITY = 0.5;
    //#region All these should be configurable in the production release
    public static final int VOLUME_DIRECTION_FLIP_DELAY = 1000;
    // Higher values will slow down volume change speed
    public static final int MUDRA_SMOOTH_FACTOR = 10;
    public static final String VIEW_ALBUMS = "Albums";
    public static final String VIEW_SONGS = "SongList";
    public static final boolean USING_MUDRA = true;
    public static final int BACK_BUTTON_SOUND_EFFECT = AudioManager.FX_KEYPRESS_SPACEBAR;
}
