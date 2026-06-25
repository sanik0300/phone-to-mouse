package com.sanikshomemade.phonetomouse.gesturedetection;

import android.os.Debug;
import android.os.Handler;
import android.util.DebugUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.sanikshomemade.phonetomouse.*;
import com.sanikshomemade.phonetomouse.bluetoothclasses.BtUtils;

public class MousePretendGestureListener extends GestureDetector.SimpleOnGestureListener {

    MouseFakingManager mouseManager;
    private boolean isLongPressNow = false;
    public boolean getLongPress() {
        return isLongPressNow;
    }
    private boolean isScrollBlocked = false;
    public boolean getScrollBlocked() {
        return  isScrollBlocked;
    }
    public void unblockScroll() {
        isScrollBlocked = false;
    }

    public MousePretendGestureListener(MouseFakingManager mouseManager) {
        this.mouseManager = mouseManager;
    }

    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        if(BtUtils.ConnectionExists()) {
            BtUtils.SendBytes(mouseManager.GetClickBytes(e.getX(), e.getY()));
        }
        return super.onSingleTapConfirmed(e);
    }

    public void OnUpAfterScrollPressed(float eventX, float eventY) {
        if(!BtUtils.ConnectionExists()) { return; }

        isLongPressNow = false;
        mouseManager.ChangeButtonPressState(false);
        BtUtils.SendBytes(mouseManager.GetMouseUpBytes(eventX, eventY));
        if(PrefUtils.getValueFromPrefs(MyApp.currentBtDependentActivity, PrefUtils.LONG_PRESS_SOUND_KEY, true)) {
            EffectsUtils.VibrateOnLongPress(MyApp.currentBtDependentActivity);
        }
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        if(BtUtils.ConnectionExists() && !isLongPressNow) {
            isLongPressNow = true;
            isScrollBlocked = true;
            mouseManager.ChangeButtonPressState(true);
            BtUtils.SendBytes(mouseManager.GetMouseDownBytes(e.getX(), e.getY()));
            if(PrefUtils.getValueFromPrefs(MyApp.currentBtDependentActivity, PrefUtils.LONG_PRESS_SOUND_KEY, true)) {
                EffectsUtils.VibrateOnLongPress(MyApp.currentBtDependentActivity);
            }
            System.out.println("sending Mouse Down");
        }
        super.onLongPress(e);
    }

    @Override
    public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {

        if(BtUtils.ConnectionExists()) {
            BtUtils.SendBytes(mouseManager.GetCursorMovementBytes(e2.getX(), e2.getY(), isLongPressNow));
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        if(BtUtils.ConnectionExists() && isLongPressNow) {
            this.OnUpAfterScrollPressed(e2.getX(), e2.getY());
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        if(BtUtils.ConnectionExists()) {
            BtUtils.SendBytes(mouseManager.GetDoubleClickBytes(e.getX(), e.getY()));
        }
        return super.onDoubleTap(e);
    }
}
