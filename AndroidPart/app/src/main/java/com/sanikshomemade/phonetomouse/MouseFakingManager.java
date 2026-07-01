package com.sanikshomemade.phonetomouse;

import android.os.Handler;
import android.os.Message;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class MouseFakingManager {

    public enum Commands {
        Move, Down, Up, Click, DoubleClick, Wheel, Ratio, Disconnect;
        private static Commands[] allValues = values();
        public static Commands fromOrdinal(int n) {return allValues[n];}
    }
    private boolean pretendedBtnIsLeft = true;
    private boolean isMouseButtonPressed = false;
    private float pseudoScreenW, pseudoScreenH;
    private float currentCursorX=-1, currentCursorY=-1;
    private Handler contextHandler;

    public MouseFakingManager(Handler contextHandler) {
        this.contextHandler = contextHandler;
    }
    public boolean isPretendedButtonLeft() { return pretendedBtnIsLeft; }
    public boolean isMouseButtonPressed() { return isMouseButtonPressed; }
    public float getPseudoScreenWidth() { return pseudoScreenW; }
    public float getPseudoScreenHeight() { return pseudoScreenH; }

    public void setPseudoScreenSize(float w, float h) {
        pseudoScreenW = w;
        pseudoScreenH = h;
    }

    private byte[] xyToRelativeInBytes(float... arr) {
        ByteBuffer buf = ByteBuffer.allocate(4*arr.length);

        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.asFloatBuffer().put(arr[0]/pseudoScreenW);
        buf.asFloatBuffer().put(1, arr[1]/pseudoScreenH);

        byte[] result = new byte[buf.remaining()];
        buf.get(result);

        return result;
    }

    public byte[] GetCursorMovementBytes(float xOnPhone, float yOnPhone, boolean anyBtnPressed)
    {
        byte[] coordinatesPart = xyToRelativeInBytes(xOnPhone, yOnPhone);
        byte[] result = new byte[coordinatesPart.length+2];
        result[0] = (byte)Commands.Move.ordinal();
        if(anyBtnPressed) {
            result[1] = (byte) (pretendedBtnIsLeft? 1 : 0);
        }
        else {
            result[1] = -1;
        }

        System.arraycopy(coordinatesPart, 0, result, 2, coordinatesPart.length);
        return result;
    }

    public void ChangePretendedMouseButton(boolean left) {
        pretendedBtnIsLeft = left;
    }
    public void ChangeButtonPressState(boolean pressed) {
        isMouseButtonPressed = pressed;

        Message msg = contextHandler.obtainMessage();
        msg.what = pressed? Commands.Down.ordinal() : Commands.Up.ordinal();
        msg.obj = pretendedBtnIsLeft;
        contextHandler.sendMessage(msg);
    }

    private byte[] getMouseKeyGestureBytes(Commands gestureFlag, boolean leftButtonUsed, float x, float y)
    {
        byte[] coordinatesPart = xyToRelativeInBytes(x, y);
        byte[] result = new byte[coordinatesPart.length+2];
        result[0] = (byte)gestureFlag.ordinal();
        result[1] = (byte)(leftButtonUsed? 1 : 0);
        System.arraycopy(coordinatesPart, 0, result, 2, coordinatesPart.length);
        return result;
    }
    public byte[] GetClickBytes(float xOnPhone, float yOnPhone) {
        return getMouseKeyGestureBytes(Commands.Click, pretendedBtnIsLeft, xOnPhone, yOnPhone);
    }
    public byte[] GetDoubleClickBytes(float xOnPhone, float yOnPhone) {
        return getMouseKeyGestureBytes(Commands.DoubleClick, pretendedBtnIsLeft, xOnPhone, yOnPhone);
    }
    public byte[] GetMouseDownBytes(float xOnPhone, float yOnPhone) {
        return getMouseKeyGestureBytes(Commands.Down, pretendedBtnIsLeft, xOnPhone, yOnPhone);
    }
    public byte[] GetMouseUpBytes(float xOnPhone, float yOnPhone) {
        return getMouseKeyGestureBytes(Commands.Up, pretendedBtnIsLeft, xOnPhone, yOnPhone);
    }

    public byte[] GetScrollWheelBytes(boolean up)
    {
        return new byte[] { (byte)Commands.Wheel.ordinal(), (byte)(up? 1 : 0) };
    }

    public static byte[] GetDisconnectConfirmationBytes(boolean responseNeeded, boolean explicit)
    {
        return new byte[] { (byte)Commands.Disconnect.ordinal(),
                            (byte)(responseNeeded? 1: 0),
                            (byte)(explicit? 1 : 0)};
    }
}
