package il.co.wearabledevices.mudramediaplayer;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

//import il.co.wearabledevices.mudramediaplayer.R;
//import il.co.wearabledevices.mudramediaplayer.utils.LogHelper;

public class MainActivity extends WearableActivity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        // Enables Always-on
        setAmbientEnabled();
    }
}
