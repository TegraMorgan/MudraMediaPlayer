package il.co.wearabledevices.mudramediaplayer.utils;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.MediaController;

import il.co.wearabledevices.mudramediaplayer.MainActivity;

public class TegraController extends MediaController {
    Context c;

    public TegraController(Context context) {
        super(context);
        c = context;
    }

    public void hide() {

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((MainActivity) c).onBackPressed();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}
