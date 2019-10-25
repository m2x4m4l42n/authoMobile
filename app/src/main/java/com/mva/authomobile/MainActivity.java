package com.mva.authomobile;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothAdapter bluetoothAdapter;
        final BluetoothLeScanner bluetoothLeScanner;
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

        bluetoothLeScanner.startScan(new BluetoothLECallback(this));

    }
}
