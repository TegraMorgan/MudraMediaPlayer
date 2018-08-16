package il.co.wearabledevices.mudramediaplayer;

import android.graphics.Color;
import android.media.AudioManager;

public class constants {
    public static final int DATA_TYPE_GESTURE = 0;
    public static final int DATA_TYPE_PROPORTIONAL = 1;
    public static final int DATA_TYPE_SNC = 2;
    public static final int DATA_TYPE_QUATERNIONS = 3;
    public static final int DATA_TYPE_ACCNORM = 4;
    public static final int DATA_TYPE_UIACTION = 5;

    public static final String SERIALIZE_ALBUM = "serialize_album";
    public static final String SERIALIZE_MUSIC_ACTIVITY = "serilaize_music_activity";
    public static final String MUSIC_ACTIVITY = "music_activity";
    public static final String PLAY_LIST = "play_list";
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
    public static final int VOLUME_DIRECTION_FLIP_DELAY = 2000;
    // Higher values will slow down volume change speed
    public static final int MUDRA_SMOOTH_FACTOR = 10;
    public static final String VIEW_ALBUMS = "Albums";
    public static final String VIEW_ACTIVITIES = "ActivitieList";
    public static final String VIEW_PLAYLISTS = "PlaylistList";
    public static final String VIEW_SONGS = "SongList";
    public static final String VIEW_NOW_PLAYING = "NowPlaying";

    public static final boolean USING_MUDRA = true;
    public static final int BACK_BUTTON_SOUND_EFFECT = AudioManager.FX_KEYPRESS_SPACEBAR;
    public static final int BACK_BUTTON_ICON = R.drawable.lb_ic_playback_loop;
    public static final int mInterval = 300; // miliseconds
    public static final int ACCEPTABLE_LENGTH = 20;
    public static final String LABEL_PLAYLISTS = "Playlists";
    public static final String LABEL_SONGS = "Songs";
    public static final String LABEL_ACTIVITIES = "Activities";
    public static final String SONG_TITLE = "song_title";
    public static final String SONG_ARTIST = "song_artist";
    public static final int SELECTOR_COLOR_GREY = Color.rgb(0x80,0x80,0x80);

}
