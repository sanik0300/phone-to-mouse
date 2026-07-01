package com.sanikshomemade.phonetomouse.bluetoothclasses;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import com.sanikshomemade.phonetomouse.activities.BluetoothConfigActivity;

public class BluetoothDevicesFindingReceiver extends BroadcastReceiver {

    private BluetoothConfigActivity _parentActivity;

    public BluetoothDevicesFindingReceiver(BluetoothConfigActivity activity) {
        _parentActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) { return; }

        BluetoothDevice foundDevice;
        if (Build.VERSION.SDK_INT >= 33) {
            foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
        } else {
            foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        }
        if (foundDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
            return;
        } //this type shall be handled by scancallback

        if (!BtUtils.CanBeComputer(foundDevice)) { return; }
        _parentActivity.OnVisibleDeviceDiscovered(foundDevice);
    }
}
