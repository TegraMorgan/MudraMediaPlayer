package il.co.wearabledevices.mudramediaplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import il.co.wearabledevices.mudramediaplayer.model.MediaLibrary;

import static il.co.wearabledevices.mudramediaplayer.constants.REQUEST_MEDIA_ACCESS;


public class PermissionRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_request);
    }

    public void requestTime(View view) {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_MEDIA_ACCESS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MEDIA_ACCESS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    //TODO Close the activity
                    MediaLibrary.buildMediaLibrary(this);
                    // Not sure if this will work
                    PermissionRequestActivity.this.finish();
                } else {
                    //If permission denied - Do nothing
                }
            }
        }
    }
}
