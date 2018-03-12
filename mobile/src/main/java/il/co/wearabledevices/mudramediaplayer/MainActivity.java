package il.co.wearabledevices.mudramediaplayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mBrowseAlbums;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBrowseAlbums = findViewById(R.id.bt_sel_albm);
mBrowseAlbums.setOnClickListener((v)-> {
    Intent intent = new Intent(MainActivity.this,AlbumSelectionActivity.class);
    startActivity(intent);
});
    }
}
