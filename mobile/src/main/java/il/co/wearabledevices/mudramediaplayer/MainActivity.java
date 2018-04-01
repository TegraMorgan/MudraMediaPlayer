package il.co.wearabledevices.mudramediaplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;
import il.co.wearabledevices.mudramediaplayer.player.PlayerActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_MEDIA_ACCESS = 4765;
    private Button mBrowseAlbums;
    private Button mBrowseArtists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mBrowseAlbums = findViewById(R.id.bt_sel_albm);
        mBrowseArtists = findViewById(R.id.bt_sel_artist);
        mBrowseAlbums.setOnClickListener((v) -> {
            Context mainContext = MainActivity.this;
            Class destinationActivity = AlbumSelectionActivity.class;
            Intent albumSelection = new Intent(mainContext, destinationActivity);
            startActivity(albumSelection);
        });
        mBrowseArtists.setOnClickListener(view -> {
            Context mainContext = MainActivity.this;
            Class destinationActivity = PlayerActivity.class;
            Intent player = new Intent(mainContext, destinationActivity);
            startActivity(player);
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            Log.v(TAG, "No permission");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_MEDIA_ACCESS);
        } else {
            //TODO Tegra Launch this on separate thread in the future
            MediaLibrary.buildMediaLibrary(this);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MEDIA_ACCESS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    //TODO Tegra Launch this on separate thread in the future
                    MediaLibrary.buildMediaLibrary(this);
                } else {
                    //permission denied
                }
                return;
            }
        }
        return;
    }
}
