package com.mva.authomobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothLEBroadcastReceiver extends BroadcastReceiver {

    public static final String RSSI = "android.le.rssi"; // int
    public static final String SCAN_RECORD_BYTES = "android.le.scanRecordBytes"; // byte[]
    public static final String TIMESTAMP_NANOS = "android.le.timestampNanos"; // long

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println(
                "Beacon empfangen: RSSI:" + intent.getIntExtra(RSSI,0)
                        + " SCANBYTES: " + intent.getByteArrayExtra(SCAN_RECORD_BYTES)
                        + " TIMESTAMP: " + intent.getLongExtra(TIMESTAMP_NANOS,0));
    }
}
