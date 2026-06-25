package com.sanikshomemade.phonetomouse.activities;

import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Build;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDelegate;
import com.sanikshomemade.phonetomouse.MyApp;
import com.sanikshomemade.phonetomouse.PrefUtils;
import com.sanikshomemade.phonetomouse.bluetoothclasses.BtUtils;
import com.sanikshomemade.phonetomouse.gesturedetection.GoToActivityListener;
import com.sanikshomemade.phonetomouse.R;

public class EntryActivity extends MyApp.BluetoothDependentCompatActivity {

    boolean bluetoothRequested = false;

    static private String langCodeSetInPref = null;
    static public void setLangCodeForEntry(String code) {
        langCodeSetInPref = code;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        if(Build.VERSION.SDK_INT >= 29) {
            int themeOption = PrefUtils.getValueFromPrefs(this, PrefUtils.THEME_OPTION, 0);
            AppCompatDelegate.setDefaultNightMode(themeOption==0? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM : themeOption);
        }
        else {
            boolean nightMode = PrefUtils.getValueFromPrefs(this, PrefUtils.THEME_OPTION, false);
            AppCompatDelegate.setDefaultNightMode(nightMode? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        }

        Button b1 = this.findViewById(R.id.go_to_touchscreen);
        b1.setOnClickListener(new GoToActivityListener(this, TouchscreenActivity.class));

        Button b2 = this.findViewById(R.id.go_to_info);
        b2.setOnClickListener(new GoToActivityListener(this, InfoActivity.class));

        Button b3 = this.findViewById(R.id.go_to_settings);
        b3.setOnClickListener(new GoToActivityListener(this, OptionsActivity.class));

        Button b4 = this.findViewById(R.id.blt_settings_btn);
        b4.setOnClickListener(new GoToActivityListener(this, BluetoothConfigActivity.class));
    }

    private void ConfigureEntryButtons(Button touchscreenBtn) {
        int targetDeviceIndx = BtUtils.RefreshPairedDevices(this);

        boolean someDeviceSaved = targetDeviceIndx != -1;
        touchscreenBtn.setEnabled(someDeviceSaved);

        TextView nameTextView = this.findViewById(R.id.target_device_name);
        nameTextView.setText(someDeviceSaved? PrefUtils.getValueFromPrefs(this, PrefUtils.LAST_PAIRED_DEVICE_NAME, "")
                                            : getResources().getString(R.string.entry_device_name_placeholder));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(langCodeSetInPref != null) {
            langCodeSetInPref = null;
            recreate();
            return;
        }

        Button b1 = this.findViewById(R.id.go_to_touchscreen);
        try {
            ConfigureEntryButtons(b1);
        }
        catch(Exception e)  {
            b1.setEnabled(false);
        }

        if(Build.VERSION.SDK_INT >= 23 && !bluetoothRequested) {
            requestPermissions(new String[]{"android.permission.BLUETOOTH_CONNECT",
                                            "android.permission.BLUETOOTH_SCAN"}, 1);
            bluetoothRequested = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int i = 0; i<permissions.length; i++) {
            if(permissions[i].equals("android.permission.BLUETOOTH_CONNECT") && grantResults[i]== PackageManager.PERMISSION_GRANTED)
            {
                ConfigureEntryButtons(this.findViewById(R.id.go_to_touchscreen));
                break;
            }
        }
    }

    @Override
    public void OnBluetoothStatusChanged(boolean connected) {
        Button configBtButton = this.findViewById(R.id.blt_settings_btn),
                touchscreenButton = this.findViewById(R.id.go_to_touchscreen);

        configBtButton.setEnabled(connected);
        touchscreenButton.setEnabled(connected && !PrefUtils.getValueFromPrefs(this, PrefUtils.LAST_PAIRED_MAC, "").equals(""));

        TextView menuHeader = this.findViewById(R.id.menu_header),
                 btDisabled = this.findViewById(R.id.no_blt_warning_textview);
        btDisabled.setVisibility(connected?  View.GONE : View.VISIBLE);
        if(connected) {
            menuHeader.setPaintFlags(menuHeader.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        else {
            menuHeader.setPaintFlags(menuHeader.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
}