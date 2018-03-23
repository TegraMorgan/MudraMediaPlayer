package il.co.wearabledevices.mudramediaplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import il.co.wearabledevices.mudramediaplayer.model.Playlist;
import il.co.wearabledevices.mudramediaplayer.service.MudraMusicService;

/**
 * Copy this activity if you need to create new activity with controls of music service
 */

public class EmptyActivityWithMusicPlayer extends AppCompatActivity {


    private MudraMusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound;
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MudraMusicService.MusicBinder binder = (MudraMusicService.MusicBinder) service;
            // get the service pointer
            musicSrv = binder.getService();
            //mark as bounded
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MudraMusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_with_music_player);
    }


    @Override
    protected void onDestroy() {
        stopService(playIntent); // Do this only if you want to stop the music when you exit the activity
        unbindService(musicConnection);
        musicSrv = null;

        super.onDestroy();
    }

    protected void StopPlayback() {
        stopService(playIntent);
        musicSrv = null;
    }

    protected void StartPlayback() {
        /* Change toPlay according to your need */
        Playlist toPlay = new Playlist();
        musicSrv.setList(toPlay);
        musicSrv.playSong();
    }
}
