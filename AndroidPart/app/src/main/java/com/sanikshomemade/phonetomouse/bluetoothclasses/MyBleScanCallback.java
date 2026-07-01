package com.sanikshomemade.phonetomouse.bluetoothclasses;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import com.sanikshomemade.phonetomouse.activities.BluetoothConfigActivity;

import java.util.List;

public class MyBleScanCallback extends ScanCallback {

    private BluetoothConfigActivity _parentActivity;
    public MyBleScanCallback(BluetoothConfigActivity activity) { _parentActivity = activity; }
    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        BluetoothDevice bd = result.getDevice();
        if (!BtUtils.CanBeComputer(bd)) { return; }

        _parentActivity.OnVisibleDeviceDiscovered(bd);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for(ScanResult res : results) {
            BluetoothDevice bd = res.getDevice();
            if (!BtUtils.CanBeComputer(bd)) { continue; }

            _parentActivity.OnVisibleDeviceDiscovered(bd);
        }
    }
}
