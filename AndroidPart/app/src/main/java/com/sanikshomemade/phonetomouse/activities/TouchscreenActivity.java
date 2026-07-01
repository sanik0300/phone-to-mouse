package com.sanikshomemade.phonetomouse.activities;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.*;
import android.view.*;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SwitchCompat;
import com.sanikshomemade.phonetomouse.*;
import com.sanikshomemade.phonetomouse.bluetoothclasses.BtUtils;
import com.sanikshomemade.phonetomouse.gesturedetection.MousePretendGestureListener;
import com.sanikshomemade.phonetomouse.gesturedetection.MyTouchListener;
import com.sanikshomemade.phonetomouse.gesturedetection.PseudoScreen;

public class TouchscreenActivity extends MyApp.BluetoothDependentCompatActivity {

    private boolean connectionRequested = false;
    public void setConnectionRequested(boolean val) {
        connectionRequested =val;
    }
    boolean screenRatioObtained=false;
    private TextView[] directionTexts;
    Handler _handler = new TouchscreenHandler(Looper.myLooper(), this);
    private final MouseFakingManager mouseManager=new MouseFakingManager(_handler);
    private final MousePretendGestureListener mainListener = new MousePretendGestureListener(this.mouseManager);
    private MyTouchListener touchListener = null;

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

    private void styleScreenForConnection() {
        ((TextView)this.findViewById(R.id.center_textview)).setText("");
        ((ImageButton)this.findViewById(R.id.stop_start_button)).setImageResource(R.drawable.pause);
        directionTexts[0].setText(R.string.screen_up);
        directionTexts[1].setText(R.string.screen_down);
        directionTexts[2].setText(R.string.screen_left);
        directionTexts[3].setText(R.string.screen_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mainView = getLayoutInflater().inflate(R.layout.activity_touchscreen, null, false);
        setContentView(mainView);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        directionTexts = new TextView[] {
                this.findViewById(R.id.text_up), this.findViewById(R.id.text_down),
                this.findViewById(R.id.text_left), this.findViewById(R.id.text_right)
        };

        PseudoScreen ps = this.findViewById(R.id.pseudo_screen);
        int orientation = this.getResources().getConfiguration().orientation;
        if (BtUtils.ConnectionExists()) {
            styleScreenForConnection();

            int psw = (int)mouseManager.getPseudoScreenWidth();
            int psh = (int)mouseManager.getPseudoScreenHeight();
            if (psw > 0 || psh > 0) {
                ViewGroup.LayoutParams lparams = ps.getLayoutParams();
                if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                    lparams.width = psh;
                    lparams.height = psw;
                }
                else {
                    lparams.width = psw;
                    lparams.height = psh;
                }
                ps.setLayoutParams(lparams);
            }
        }

        if (orientation != Configuration.ORIENTATION_PORTRAIT) {
            ps.getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {
                    if (screenRatioObtained) { return; }
                    mouseManager.setPseudoScreenSize(ps.getWidth(), ps.getHeight());
                }
            );
        }
        this.touchListener = new MyTouchListener(new MyGestureDetector(this, mainListener));
        ps.setOnTouchListener(touchListener);

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

                    styleScreenForConnection();

                    new Thread(new BtUtils.SocketListeningRunnable(_handler)).start();
                }
            }
        });

        SwitchCompat _switch = this.findViewById(R.id.switch_right_left);
        _switch.setZ(((ViewGroup)_switch.getParent()).getChildCount());

        _switch.setChecked(mouseManager.isPretendedButtonLeft());
        IndicateSelectedPretendedButtonState(mouseManager.isPretendedButtonLeft(), false);
        IndicateUnselectedPretendedButtonState(mouseManager.isPretendedButtonLeft());

        _switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mouseManager.ChangePretendedMouseButton(_switch.isChecked());

                IndicateSelectedPretendedButtonState(_switch.isChecked(), false);
                IndicateUnselectedPretendedButtonState(_switch.isChecked());
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
    private void IndicateUnselectedPretendedButtonState(boolean selIsleft) {
        TextView other = findViewById(selIsleft? R.id.right_btn_view :  R.id.left_btn_view);
        if(Build.VERSION.SDK_INT >= 23) {
            other.setTextAppearance(R.style.MouseButtonView);
        }
        else {
            other.setTextAppearance(TouchscreenActivity.this, R.style.MouseButtonView);
        }
        other.setBackground(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(connectionRequested) {
            ImageButton btn = this.findViewById(R.id.stop_start_button);
            btn.callOnClick();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!BtUtils.ConnectionExists()) { return; }
        BtUtils.SendBytes(MouseFakingManager.GetDisconnectConfirmationBytes(false, false));
        BtUtils.Disconnect(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        MyApp.currentBtDependentActivity = this;

        if (mouseManager.isMouseButtonPressed()) {
            mainListener.OnUpAfterScrollPressed(touchListener.GetLastTouchEventX(), touchListener.GetLastTouchEventY());
        }

        ViewGroup rootLayout = (ViewGroup) this.findViewById(android.R.id.content);
        rootLayout.removeAllViews();
        this.setContentView(R.layout.activity_touchscreen);
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