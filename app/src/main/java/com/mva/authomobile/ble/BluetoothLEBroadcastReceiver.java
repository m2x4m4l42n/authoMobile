package com.mva.authomobile.ble;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.mva.authomobile.data.BeaconManager;
import com.mva.authomobile.service.MainService;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLEBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "BLEBroadcastReceiver";

    public static final String SCAN_RESULT = "android.le.scanResult"; // ScanResult
    public static final String CALLBACK_TYPE = "android.le.callbackType"; // int

    @Override
    public void onReceive(Context context, Intent intent) {

        ScanResult scanResult = intent.getParcelableExtra(SCAN_RESULT);
        Log.i(TAG, "onReceive: "+ scanResult.getScanRecord().toString());


        if(scanResult != null){
            final Intent serviceIntent = new Intent(context, MainService.class);
            serviceIntent.putExtra(SCAN_RESULT, scanResult);
            context.startService(serviceIntent);
        }


    }

}
