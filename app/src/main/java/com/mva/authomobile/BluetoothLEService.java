package com.mva.authomobile;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLEService extends Service {

    private BluetoothLECallback callback;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;


    public BluetoothLEService() {

        // check system feature
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            //TODO: add behaviour in case BLE is not present in phone
        }

        // setup bluetooth adapter
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //check bluetooth availability and status
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBLEIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        }
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /***
     *
     * Startup the service and setup bluetooth LE
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startScan();
        return super.onStartCommand(intent, flags, startId);
    }

    public void startScan(){
        bluetoothLeScanner.startScan(getCallback());

    }
    public void stopScan(){
        bluetoothLeScanner.stopScan(getCallback());
    }

    public ScanCallback getCallback(){
           if(callback == null){
               callback = new BluetoothLECallback(this);
           }
           return callback;
    }

}
