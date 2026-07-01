package com.sanikshomemade.phonetomouse.bluetoothclasses;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import com.sanikshomemade.phonetomouse.EffectsUtils;
import com.sanikshomemade.phonetomouse.PrefUtils;
import com.sanikshomemade.phonetomouse.R;

import java.io.*;
import java.util.*;

public class BtUtils {

    static public class SocketListeningRunnable extends BluetoothListeningRunnable {
        public SocketListeningRunnable(Handler uiHandler) {
            super(uiHandler);
            this.socket = BtUtils.socket;
        }
    }
    static BluetoothSocket socket;
    static int targetDeviceIndex = -1;
    static public int getTargetDeviceIndex() {
        return targetDeviceIndex;
    }
    static ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
    static BluetoothAdapter _adapter;
    static private BluetoothAdapter getAdapter(Context context) {
        if (_adapter == null) {
            _adapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        }
        return _adapter;
    }
    static public boolean IsBluetoothEnabled(Context ct) {
        getAdapter(ct);
        return _adapter != null && _adapter.isEnabled();
    }
    static public boolean ConnectionExists() { return socket!=null && socket.isConnected(); }
    static public BluetoothLeScanner getScanner(Context context) {
        return getAdapter(context).getBluetoothLeScanner();
    }
    static public List<BluetoothDevice> getListOfPaired() { return pairedDevices; }

    static public int RefreshPairedDevices(Context ct) {
        Set<BluetoothDevice> setOfDevices = getAdapter(ct).getBondedDevices();
        pairedDevices.clear();
        for (BluetoothDevice bd : setOfDevices) { //not stream because min sdk 21 doesnt support them
            if (CanBeComputer(bd)) {
                pairedDevices.add(bd);
            }
        }

        String savedMac = PrefUtils.getValueFromPrefs(ct, PrefUtils.LAST_PAIRED_MAC, "");
        int counter = 0;
        for(BluetoothDevice bd : pairedDevices) {
            if(bd.getAddress().equals(savedMac)) {
                targetDeviceIndex = counter;
                return counter;
            }
            counter++;
        }
        return -1;
    }

    static public void SelectDeviceToConnectByIndex(int index) {
        targetDeviceIndex = index;
    }

    static public void BeginBluetoothDiscovery(Context ct) {
        getAdapter(ct).startDiscovery();
    }
    static public void CancelBluetoothDiscovery(Context ct) {
        getAdapter(ct).cancelDiscovery();
    }


    static public boolean Connect(Context ct) {
        if (socket != null && socket.isConnected()) {
            return true;
        }

        try {
            InputStream inputStream = ct.getResources().openRawResource(R.raw.guid);
            BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
            String uuidStr = bfr.readLine();

            UUID myUuid = UUID.fromString(uuidStr);
            socket = pairedDevices.get(targetDeviceIndex).createRfcommSocketToServiceRecord(myUuid);
        } catch (IOException e) { }

        try {
            socket.connect();
            if(PrefUtils.getValueFromPrefs(ct, PrefUtils.CONNECT_SOUND_KEY, false)) {
                EffectsUtils.PlayConnectionChangedSound(ct, true);
            }
            return true;
        } catch (IOException e) {
            Toast.makeText(ct, R.string.screen_no_conn_toast_text, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    static public boolean Disconnect(Context ct) {
        if(!socket.isConnected()) {
            return true;
        }

        try {
            socket.close();
            if(PrefUtils.getValueFromPrefs(ct, PrefUtils.CONNECT_SOUND_KEY, false)) {
                EffectsUtils.PlayConnectionChangedSound(ct, false);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    static public void SendBytes(byte[] bytes) {
        OutputStream outputStream = null;
        try {
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Error occurred when creating output stream");
            return;
        }

        try {
            if (!socket.isConnected()) {
                socket.connect();
            }
            outputStream.write(bytes);

            //System.out.println("written successfully");
        } catch (IOException e) {
            System.out.println("failed sending data");
            e.printStackTrace();
        }
    }

    static public boolean CanBeComputer(BluetoothDevice device) {
        int majorClass = device.getBluetoothClass().getMajorDeviceClass();

        switch (majorClass) {
            case BluetoothClass.Device.Major.COMPUTER:
            case BluetoothClass.Device.Major.MISC:
            case BluetoothClass.Device.Major.UNCATEGORIZED:
                return true;
        }
        return false;
    }
}
