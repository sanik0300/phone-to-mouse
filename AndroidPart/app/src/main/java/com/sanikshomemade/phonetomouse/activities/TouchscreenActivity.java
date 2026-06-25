package com.sanikshomemade.phonetomouse.activities;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.view.*;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.sanikshomemade.phonetomouse.*;
import com.sanikshomemade.phonetomouse.bluetoothclasses.BtUtils;
import com.sanikshomemade.phonetomouse.gesturedetection.MousePretendGestureListener;
import com.sanikshomemade.phonetomouse.gesturedetection.MyTouchListener;
import com.sanikshomemade.phonetomouse.gesturedetection.PseudoScreen;
import org.w3c.dom.Text;

public class TouchscreenActivity extends MyApp.BluetoothDependentCompatActivity {

    private boolean connectionRequested = false;
    public void setConnectionRequested(boolean val) {
        connectionRequested =val;
    }
    boolean screenRatioObtained=false;
    private TextView[] directionTexts;
    Handler _handler = new TouchscreenHandler(Looper.myLooper(), this);
    private MouseFakingManager mouseManager=new MouseFakingManager(_handler);

    public static class MyGestureDetector extends GestureDetector {
        MousePretendGestureListener _lst;
        public MousePretendGestureListener getListener() {
            return _lst;
        }
        public MyGestureDetector(Context context, OnGestureListener listener) {
            super(context, listener);
            this._lst = (MousePretendGestureListener)listener;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.activity_touchscreen, null, false);
        setContentView(mainView);

        directionTexts = new TextView[] {
                this.findViewById(R.id.text_up), this.findViewById(R.id.text_down),
                this.findViewById(R.id.text_left), this.findViewById(R.id.text_right)
        };

        PseudoScreen ps = this.findViewById(R.id.pseudo_screen);
        ps.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {
                    if(screenRatioObtained) { return; }
                    mouseManager.setPseudoScreenSize(ps.getWidth(), ps.getHeight());
                }
        );
        MousePretendGestureListener listener = new MousePretendGestureListener(this.mouseManager);
        ps.setOnTouchListener(new MyTouchListener(new MyGestureDetector(this, listener)));

        AppCompatImageButton backBtn = this.findViewById(R.id.back_button);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)v.getContext()).finish();
            }
        });
        AppCompatImageButton connBtn = this.findViewById(R.id.stop_start_button);
        connBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(BtUtils.ConnectionExists()) { //disconnect
                    connBtn.setImageResource(R.drawable.play);
                    BtUtils.SendBytes(MouseFakingManager.GetDisconnectConfirmationBytes(true, true));
                }
                else { //connect
                    boolean connectSuccessful = BtUtils.Connect(v.getContext());
                    if(!connectSuccessful) { return; }
                    connectionRequested = true;

                    connBtn.setImageResource(R.drawable.pause);
                    ((TextView)TouchscreenActivity.this.findViewById(R.id.center_textview)).setText("");
                    ((TextView)TouchscreenActivity.this.findViewById(R.id.text_up)).setText(R.string.screen_up);
                    ((TextView)TouchscreenActivity.this.findViewById(R.id.text_down)).setText(R.string.screen_down);
                    ((TextView)TouchscreenActivity.this.findViewById(R.id.text_left)).setText(R.string.screen_left);
                    ((TextView)TouchscreenActivity.this.findViewById(R.id.text_right)).setText(R.string.screen_right);

                    new Thread(new BtUtils.SocketListeningRunnable(_handler)).start();
                }
            }
        });

        SwitchCompat _switch = this.findViewById(R.id.switch_right_left);
        _switch.setZ(((ViewGroup)_switch.getParent()).getChildCount());
        _switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mouseManager.ChangePretendedMouseButton(_switch.isChecked());

                IndicateSelectedPretendedButtonState(_switch.isChecked(), false);

                TextView other = TouchscreenActivity.this.findViewById(_switch.isChecked()? R.id.right_btn_view :  R.id.left_btn_view);
                if(Build.VERSION.SDK_INT >= 23) {
                    other.setTextAppearance(R.style.MouseButtonView);
                }
                else {
                    other.setTextAppearance(TouchscreenActivity.this, R.style.MouseButtonView);
                }
                other.setBackground(null);
            }
        });
    }

    public void IndicateSelectedPretendedButtonState(boolean left, boolean pressed) {
        TextView targetTv = findViewById(left? R.id.left_btn_view : R.id.right_btn_view);
        if(Build.VERSION.SDK_INT >= 23) {
            targetTv.setTextAppearance(pressed? R.style.MouseButtonView : R.style.SelectedMouseButtonView);
        }
        else {
            targetTv.setTextAppearance(this, pressed? R.style.MouseButtonView : R.style.SelectedMouseButtonView);
        }
        targetTv.setBackground(getResources()
                .getDrawable(pressed? R.drawable.mouse_textview_bg_pressed : R.drawable.mouse_textview_border, this.getTheme()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(connectionRequested) {
            ImageButton btn = this.findViewById(R.id.stop_start_button);
            btn.callOnClick();
        }

        /*((AppCompatImageButton)this.findViewById(R.id.stop_start_button))
                .setImageResource(connectionRequested? R.drawable.pause : R.drawable.play);
        if(connectionRequested) {
            BtUtils.Connect(this);
        }
        else {
            showSingleTextOnScreen(R.string.screen_you_can_connect);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!BtUtils.ConnectionExists()) { return; }
        BtUtils.SendBytes(MouseFakingManager.GetDisconnectConfirmationBytes(false, false));
        BtUtils.Disconnect(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(BtUtils.ConnectionExists()) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    BtUtils.SendBytes(mouseManager.GetScrollWheelBytes(false));
                    break;
                case KeyEvent.KEYCODE_VOLUME_UP:
                    BtUtils.SendBytes(mouseManager.GetScrollWheelBytes(true));
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showSingleTextOnScreen(int stringResId) {
        for(TextView tv : directionTexts) {
            tv.setText("");
        }
        TextView centerTv = this.findViewById(R.id.center_textview);
        centerTv.setText(getResources().getString(stringResId));
    }
    @Override
    public void OnBluetoothStatusChanged(boolean connected) {
        ImageButton playpausebtn = this.findViewById(R.id.stop_start_button);
        playpausebtn.setEnabled(connected);

        if(connected) {
            if(PrefUtils.getValueFromPrefs(this, PrefUtils.PREFERRED_BT_RECONNECT, false)) {
                playpausebtn.callOnClick();
                return;
            }
            else {
                showSingleTextOnScreen(R.string.screen_you_can_connect);
            }
        }
        else {
            showSingleTextOnScreen(R.string.screen_no_blt);
        }
        playpausebtn.setImageResource(R.drawable.play);
    }
}