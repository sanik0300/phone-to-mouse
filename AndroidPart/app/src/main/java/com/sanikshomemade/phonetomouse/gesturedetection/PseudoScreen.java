package com.sanikshomemade.phonetomouse.gesturedetection;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.View;
import com.sanikshomemade.phonetomouse.PrefUtils;
import com.sanikshomemade.phonetomouse.R;

/**
 * TODO: document your custom view class.
 */
public class PseudoScreen extends View {

    private Bitmap cursorBmp;
    private MyTouchListener _listener;

    private void init(Context ctx) {
        cursorBmp = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.cursor);
        int bmpScaleFactor = PrefUtils.getValueFromPrefs(this.getContext(), PrefUtils.PREFERRED_CURSOR_SCALE, 20);
        cursorBmp = Bitmap.createScaledBitmap(cursorBmp,
                                      cursorBmp.getWidth()/bmpScaleFactor,
                                      cursorBmp.getHeight()/bmpScaleFactor, false);
    }

    public void updateView() {
        invalidate();
    }

    public PseudoScreen(Context context) {
        super(context);
        init(context);
    }

    public PseudoScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PseudoScreen(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
        if(MyTouchListener.class.isInstance(l)) {
            _listener = (MyTouchListener)l;
        }
    }

    private float clamp(float lower, float val, float upper)
    {
        return Math.min(Math.max(lower, val), upper);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!cursorBmp.isRecycled() && !(_listener.GetLastTouchEventX()<0 && _listener.GetLastTouchEventY()<0))
        {
            canvas.drawBitmap(cursorBmp,
                    clamp(0, _listener.GetLastTouchEventX(), this.getWidth()),
                    clamp(0, _listener.GetLastTouchEventY(), this.getHeight()), null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cursorBmp.recycle();
    }
}