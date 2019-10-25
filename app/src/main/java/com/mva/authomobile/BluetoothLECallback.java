package com.mva.authomobile;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.List;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLECallback extends ScanCallback {

    private Context context;

    public BluetoothLECallback(Context context){
        super();
        this.context = context;

    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);

        final Intent intent = new Intent(this.context, BluetoothLEBroadcastReceiver.class);
        intent.putExtra(BluetoothLEBroadcastReceiver.RSSI, result.getRssi());
        intent.putExtra(BluetoothLEBroadcastReceiver.SCAN_RECORD_BYTES, result.getScanRecord().getBytes());
        intent.putExtra(BluetoothLEBroadcastReceiver.TIMESTAMP_NANOS, result.getTimestampNanos());
        sendIntent(intent);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        super.onBatchScanResults(results);
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        switch(errorCode){
            case SCAN_FAILED_ALREADY_STARTED:
                break;
            case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                break;
            case SCAN_FAILED_INTERNAL_ERROR:
                break;
            case SCAN_FAILED_FEATURE_UNSUPPORTED:
                break;

                default:


        }
    }

    private void sendIntent(Intent intent){
        context.sendBroadcast(intent);
    }
}
