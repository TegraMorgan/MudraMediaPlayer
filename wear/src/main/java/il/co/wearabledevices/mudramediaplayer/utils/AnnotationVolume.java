package il.co.wearabledevices.mudramediaplayer.utils;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationVolume {
    public static final int IDLE = 0;
    public static final int P1 = 1;
    public static final int P2 = 2;
    public static final int P3 = 3;
    public static final int PM1 = -1;
    public static final int PM2 = -2;
    public static final int PM3 = -3;
    public int state;

    public AnnotationVolume(@VolumeState int state) {
        this.state = state;
    }

    @IntDef({IDLE, P1, P2, P3, PM1, PM2, PM3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VolumeState {
    }
}
