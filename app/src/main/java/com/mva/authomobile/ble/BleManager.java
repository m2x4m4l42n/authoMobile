package com.mva.authomobile.ble;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 *  Manager class that is used to manage BLE connectivity. All manager classes implement the singleton pattern to make them
 *  easily accessible from all contexts and to prevent multiple instantiation
 */
public class BleManager implements Runnable{

    private static final String TAG = "BleManager";

    private static BleManager instance;
    private static final long SCAN_PERIOD = 60000;
    private Context context;
    private List<ScanFilter> scanFilters;
    private ScanSettings scanSettings;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothLECallback callback;
    private boolean scanning;
    private Handler scanHandler;
    private long scanInterval;

    public static BleManager getInstance(Context context){
        if(instance == null)
            instance = new BleManager();
        instance.context = context;
        return instance;
    }

    public BleManager addFilter(ScanFilter filter){
        if(scanFilters == null)
            scanFilters = new ArrayList<>();
        scanFilters.add(filter);

        return this;
    }

    public BleManager setScanSettings(ScanSettings settings){
        this.scanSettings = settings;
        return this;
    }
    public static ScanSettings makeDefaultScanSettings(){
        return new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .build();

    }
    public boolean startPeriodicScan(final long scanInterval){

        if(callback == null)
            callback = new BluetoothLECallback(context);

        if(setUpLE()){
            this.scanInterval = scanInterval;
            if(scanHandler == null) scanHandler = new Handler();
            scanHandler.post(this);
        }
        return false;
    }
    public void stopScan(){
        if(scanHandler != null)
            scanHandler.removeCallbacks(this);
    }


    private boolean setUpLE(){
        if(bluetoothLeScanner != null) return true;

        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        //check bluetooth availability and status
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            return false;

        }else {
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            return true;
        }

    }

    @Override
    public void run() {
        if(scanning){
            scanning = false;
            bluetoothLeScanner.stopScan(callback);
            scanHandler.postDelayed(this, scanInterval);
            Log.i(TAG, "run: Scan stopped");
        }else{
            scanning = true;
            bluetoothLeScanner.startScan(scanFilters, scanSettings,callback);
            scanHandler.postDelayed(this, SCAN_PERIOD);
            final StringBuilder builder = new StringBuilder();
            final byte[] manu = scanFilters.get(0).getManufacturerData();
            for(byte b : manu){
                builder.append(String.format("%02X ", (b & 0xFF)));
            }
            Log.i(TAG, "run: Scan started with ManufacturerSpecificDataMask: " + builder.toString());
        }
    }
}
