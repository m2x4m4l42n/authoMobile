package com.mva.authomobile.ble;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;



@TargetApi(Build.VERSION_CODES.LOLLIPOP)

/**
 * Callback class that extends the scan callback class of the android ble api and sends broadcasts whenever a scan result is presented
 */
public class BluetoothLECallback extends ScanCallback {

    private static final String TAG = "BluetoothLECallback";
    private Context context;

    private synchronized Context getContext(){
        return context;
    }
    private synchronized void setContext(Context context){
        this.context = context;
    }


    BluetoothLECallback(Context context){
        setContext(context);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {

        super.onScanResult(callbackType, result);
        BleManager.getInstance(context.getApplicationContext()).restartTimeout();
        final Intent intent = new Intent(getContext(), BluetoothLEBroadcastReceiver.class);
        intent.putExtra(BluetoothLEBroadcastReceiver.CALLBACK_TYPE,callbackType);
        intent.putExtra(BluetoothLEBroadcastReceiver.SCAN_RESULT, result);
        sendIntent(intent);

    }

    @Override
    public void onScanFailed(int errorCode) {

        // TODO: 2019-11-08 impelent Error behaviour
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

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(this.context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        try{
            pendingIntent.send();
        }catch (PendingIntent.CanceledException e){
            Log.e(TAG, "sendIntent: Cancelled Broadcast", e);
        }

    }
}
