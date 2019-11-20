package com.mva.authomobile.data;

import android.annotation.TargetApi;
import android.app.Application;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.mva.authomobile.service.MainService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BeaconManager {

    private static final String TAG = "BeaconManager";
    public static final int PROTOCOLID = 1431655765;
    public static final int MANUFACTURERID = 76;
    private static final byte[] IBEACONIDENTIFIERMASK = new byte[] {
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
            0 };

    private static BeaconManager instance;

    public static BeaconManager getInstance(Context context){
        if(instance == null)
            instance = new BeaconManager(context);

        instance.context = context;
        return instance;
    }

    private HashMap<Short, Beacon> beaconStorage;
    private Context context;

    private BeaconManager(Context context){
        beaconStorage = new HashMap<>();
        this.context = context;
    }

    public void onScanResult(ScanResult scanResult){
        byte[] result = scanResult.getScanRecord().getManufacturerSpecificData(MANUFACTURERID);
        if(result != null && result.length == 23) {
            try {
                Beacon beacon = new Beacon(scanResult);
                Log.i(TAG, "onScanResult: IBeacon Format " + beacon.print());
                putBeacon(beacon);
            }catch (Beacon.BeaconMalformedException e){
                Log.e(TAG, "onScanResult: Beacon malformed");
            }
        }else
            Log.i(TAG, "onScanResult: No IBeacon Data detected");
    }

    public static ScanFilter getScanFilter(){
        final ScanFilter.Builder builder = new ScanFilter.Builder();
        byte[] protocolBytes = new byte[23];
        ByteBuffer buffer = ByteBuffer.allocate(23);
        buffer.putInt(PROTOCOLID);
        buffer.put(new byte[]{
                0,0,
                0,0,
                0,0,
                0,0,0,0,0,0,0,0,
                //Major
                0,0,
                //Minor
                0,0,

                0});
        buffer.flip();
        buffer.get(protocolBytes);

        builder.setManufacturerData(MANUFACTURERID,protocolBytes,IBEACONIDENTIFIERMASK);
        return builder.build();
    }
    public static List<ScanFilter> makeProtocolFilters(){
        List filters = new ArrayList<ScanFilter>();
        filters.add(getScanFilter());
        return filters;

    }

    private synchronized BeaconManager putBeacon(Beacon beacon) {

        final Intent intent = new Intent(context, MainService.class);
        intent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_NEW_BEACON);
        context.startService(intent);

        if (beaconStorage.containsKey(beacon.getStationID())) {
            if (beaconStorage.get(beacon.getStationID()).getTimeNanos() < beacon.getTimeNanos()) {
                beaconStorage.remove(beacon.getStationID());
                beaconStorage.put(beacon.getStationID(),beacon);
                Log.i(TAG, "putBeacon: Updated Beacon");
            }
        }else{
            beaconStorage.put(beacon.getStationID(),beacon);
            Log.i(TAG, "putBeacon: Put new Beacon");
        }
        Log.i(TAG, "putBeacon: " +System.currentTimeMillis() + " " + getClosestBeacon().print());
        return this;
    }

    public synchronized Beacon getClosestBeacon(){

        Iterator<Beacon> beaconIterator = beaconStorage.values().iterator();
        Beacon next, nearest;
        long time = System.currentTimeMillis();
        try {
            nearest = beaconIterator.next();

            while (beaconIterator.hasNext()) {

                next = beaconIterator.next();

                if (next != null && nearest.getRssi() < next.getRssi() && (time-(next.getTimeNanos())) < 1000000)
                    nearest = next;
            }

        }catch(NoSuchElementException e){

            Log.e(TAG, "getClosestBeacon: no Such Element", e);
            return null;

        }

        return nearest;

    }


}
