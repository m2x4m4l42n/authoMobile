package com.mva.authomobile;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanFilter;
import android.os.Build;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLEUtils {

    public static final int MANUFACTURERID = 76;
    public static final byte[] IBEACONINDENTIFIER = new byte[] {

            0,0,

            //UUID
            0,0,0,0,
            0,0,
            0,0,
            0,0,0,0,0,0,0,0,
            //Major
            0,0,
            //Minor
            0,0,

            0
    };
    public static final byte[] IBEACONIDENTIFIERMASK = new byte[] {

            0,0,

            //UUID
            1,1,1,1,
            0,0,
            0,0,
            0,0,0,0,0,0,0,0,
            //Major
            0,0,
            //Minor
            0,0,

            0
    };

    public static ScanFilter getScanFilter(){

        final ScanFilter.Builder builder = new ScanFilter.Builder();
        builder.setManufacturerData(MANUFACTURERID,IBEACONINDENTIFIER,IBEACONIDENTIFIERMASK);
        return builder.build();
    }

}
