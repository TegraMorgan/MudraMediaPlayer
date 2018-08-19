package il.co.wearabledevices.mudramediaplayer.activities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import il.co.wearabledevices.mudramediaplayer.constants;
import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.model.Song;

public class MudraMusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = MudraMusicService.class.getSimpleName();
    private static final int NOTIFY_ID = 475;
    private static final String NOTIF_CHANN_ID = "MudraPlayer";

    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    /**
     * Notification manager
     */
    NotificationManager notMan;
    /**
     * Current playlist
     */
    private Playlist nowPlaying;

    /**
     * Current song
     */
    private Song currentSong;
    /**
     * Current position in playlist
     */
    private int songNum;
    private final IBinder musicBind = new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        songNum = 0;
        mMediaPlayer = new MediaPlayer();
        notMan = this.getSystemService(NotificationManager.class);
        mAudioManager = this.getSystemService(AudioManager.class);

        initMusicPlayer();
    }

    public void initMusicPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            AudioAttributes attr = new AudioAttributes.Builder().
                    setUsage(AudioAttributes.USAGE_MEDIA).
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
            AudioFocusRequest req = null;
            req = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attr)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false)
                    .setOnAudioFocusChangeListener(this, null)
                    .build();
            mMediaPlayer.setAudioAttributes(attr);
            int res = mAudioManager.requestAudioFocus(req);
        } else {
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
    }

    public void enqueuePlaylist(Playlist pl) {
        nowPlaying = pl;
        songNum = 0;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //Focus gained - if not playing resume playing
                mMediaPlayer.setVolume(1f, 1f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //Focus lost because there is another music app running - no resume
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                //Focus lost so we pause playback
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //Focus lost so lower the volume
                mMediaPlayer.setVolume(0.3f, 0.3f);
                break;
            default:
                break;
        }

    }

    public void beforeMainActivityUnbind() {
        mMediaPlayer.setOnCompletionListener(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "onCompletion: MusicService");
        if (mMediaPlayer.getCurrentPosition() > 0) {
            playNext(!constants.USING_MUDRA);
        }
    }

    public class MusicBinder extends Binder {
        MudraMusicService getService() {
            return MudraMusicService.this;
        }

        void setCallback(MediaPlayer.OnCompletionListener lis) {
            mMediaPlayer.setOnCompletionListener(lis);

        }
    }

    public void playSong() {
        mMediaPlayer.reset();
        currentSong = nowPlaying.getSongs().get(songNum);
        long currSongId = currentSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSongId);
        try {
            mMediaPlayer.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.v(TAG, "Error setting data source of:" + currentSong.getTitle() + " by " + currentSong.getArtist());
        }
        mMediaPlayer.prepareAsync();
    }

    public void jumpStartVolume() {
        //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 10, 0);
    }

    public void setVolume(int newVol) {
        newVol = newVol < 0 ? 0 : newVol;
        newVol = newVol > 15 ? 15 : newVol;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, AudioManager.FLAG_SHOW_UI);
    }

    public void adjustVolume(int direction, int flags) {
        if (direction == -1 || direction == 1)
            //TODO show ui when state can be saved
            //getMMPS().mediaControllerCompat.adjustVolume(direction, AudioManager.FLAG_SHOW_UI||flags);
            mAudioManager.adjustVolume(direction, 0);
    }

    public int getCurrentVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public void setNowPlayingPosition(int i) {
        songNum = i;
    }

    public int getPositionInSong() {
        return mMediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    public void pausePlayer() {
        mMediaPlayer.pause();
    }

    public void seek(int posn) {
        mMediaPlayer.seekTo(posn);
    }

    public void startPlayer() {
        mMediaPlayer.start();
    }

    public void playPrev(boolean usingMudra) {
        if (songNum > 0) {
            songNum--;
            //check if the current object is back and if we're no at the end of the list
            if (usingMudra) {
                if (nowPlaying.getSongs().get(songNum).getId() != constants.BACK_BUTTON_SONG_ID) {
                    playSong();
                } else {
                    mAudioManager.playSoundEffect(constants.BACK_BUTTON_SOUND_EFFECT, 1f);
                }
            } else {
                if (nowPlaying.getSongs().get(songNum).getId() == constants.BACK_BUTTON_SONG_ID) {
                    songNum--;
                }
                playSong();
            }
        } else {
            Log.i("current position", "reached the beginning");
        }
    }

    public void playNext(boolean usingMudra) {
        songNum = (songNum + 1) % nowPlaying.getSongsCount();
        if (usingMudra) {
            if (nowPlaying.getSongs().get(songNum).getId() != constants.BACK_BUTTON_SONG_ID) {
                playSong();
            } else {
                mAudioManager.playSoundEffect(constants.BACK_BUTTON_SOUND_EFFECT, 1f);
            }
        } else {
            if (nowPlaying.getSongs().get(songNum).getId() == constants.BACK_BUTTON_SONG_ID) {
                songNum = (songNum + 1) % nowPlaying.getSongsCount();
            }
            playSong();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification not = makeNotification(pendInt);

        startForeground(NOTIFY_ID, not);
    }

    public Notification makeNotification(PendingIntent pendInt) {
        NotificationChannel notCha;
        Notification.Builder builder;

        String mainTitle = currentSong.getTitle();
        String secondTitle = "secondTitle";
        String thirdTitle = currentSong.getArtist();


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notCha = new NotificationChannel(NOTIF_CHANN_ID, "MudraChannel", NotificationManager.IMPORTANCE_DEFAULT);
            notMan.createNotificationChannel(notCha);
            builder = new Notification.Builder(this, NOTIF_CHANN_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentIntent(pendInt)
                .setSmallIcon(Icon.createWithBitmap(currentSong.getAlbumArt(getApplicationContext())))
                .setTicker(secondTitle)
                .setOngoing(true)
                .setContentTitle(mainTitle)
                .setContentText(thirdTitle);
        return builder.build();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        super.onDestroy();
    }

    public Playlist getNowPlaying() {
        return nowPlaying;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public int getPlaylistPos() {
        return songNum;
    }

}
