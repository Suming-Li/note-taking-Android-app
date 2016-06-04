package edu.coen268.myproject.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Cite from http://www.it165.net/pro/html/201502/33904.html
 */
public class SketchView extends View {
    float preX, preY;
    private Path path;
    public Paint paint = null;
    Bitmap cacheBitmap = null;
    Canvas cacheCanvas = null;
    final int VIEW_WIDTH = 450;
    final int VIEW_HEIGHT = 550;

    public SketchView(Context context, AttributeSet set) {
        super(context, set);
        path = new Path();

        cacheBitmap = Bitmap.createBitmap(VIEW_WIDTH, VIEW_HEIGHT, Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();
        cacheCanvas.setBitmap(cacheBitmap);

        paint = new Paint(Paint.DITHER_FLAG); // create
        paint.setColor(Color.CYAN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //initial position
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = x;
                preY = y;
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.quadTo(preX, preY, x, y);
                preX = x;
                preY = y;
                break;
            case MotionEvent.ACTION_UP:
                cacheCanvas.drawPath(path, paint);
                path.reset();
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint painting = new Paint();
        canvas.drawBitmap(cacheBitmap, 0, 0, painting);
        canvas.drawPath(path, paint);
    }
}
