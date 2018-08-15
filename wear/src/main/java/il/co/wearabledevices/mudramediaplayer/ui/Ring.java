package il.co.wearabledevices.mudramediaplayer.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;

import il.co.wearabledevices.mudramediaplayer.R;

class Ring extends View {
    private Bitmap back;
    private Bitmap ring;
    private RectF oval;
    private Paint arcPaint;

    public Ring(Context context) {
        super(context);
        Resources res = getResources();
        back = BitmapFactory.decodeResource(res, R.drawable.button_bg_round);
        ring = BitmapFactory.decodeResource(res, R.drawable.volume_indicator_round);
        arcPaint = new Paint();
        arcPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        oval = new RectF(-1, -1, ring.getWidth() + 1, ring.getHeight() + 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawARGB(0xaa, 0, 255, 0);
        canvas.drawBitmap(back, 0, 0, null);
        canvas.saveLayer(oval, null);
        canvas.drawBitmap(ring, 0, 0, null);
        float angle = 300;
        canvas.drawArc(oval, angle - 90, 360 - angle, true, arcPaint);
        canvas.restore();
    }
}
