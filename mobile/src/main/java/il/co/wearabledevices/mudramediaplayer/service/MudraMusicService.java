package il.co.wearabledevices.mudramediaplayer.service;

import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.model.Song;

/**
 * Created by tegra on 14/03/18.
 */

public class MudraMusicService extends android.app.Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private static final String TAG = "MusicService";
    private final IBinder musicBind = new MusicBinder();
    private MediaPlayer player;
    private Playlist nowPlaying;


    @Override
    public void onCreate() {
        super.onCreate();
        nowPlaying = new Playlist();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        AudioAttributes mediaPlayerAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();
        player.setAudioAttributes(mediaPlayerAttributes);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {

        nowPlaying.songs = theSongs;
    }

    public void setList(Playlist thePlaylist) {
        nowPlaying = thePlaylist;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        player = null;
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    public void playSong() {
        player.reset();
        Song s = nowPlaying.getCurrent();
        long cs = s.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cs);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("MUSIC SERVICE", "Error setting data source");
        }
        player.prepareAsync();
    }

    public void setSong(int songIndex) {
        nowPlaying.setPosition(songIndex);
    }

    public class MusicBinder extends Binder {
        public MudraMusicService getService() {
            return MudraMusicService.this;
        }
    }

}
