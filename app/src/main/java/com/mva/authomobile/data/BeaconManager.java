package com.mva.authomobile.data;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.mva.authomobile.service.MainService;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)

/*
 * Manager class implementing the main data management of received beacon data
 */
public class BeaconManager {

    private static final String TAG = "BeaconManager";

    public static final String STATIONID_IDENTIFIER = "com.mva.authomobile.data.stationid";
    public static final int PROTOCOLID = 1431655765;
    public static final int MANUFACTURERID = 76;
    public static final long BEACON_ELAPSED_TIME_THRESHHOLD_NANOS = 3000000000L;
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

        removeOutdatedBeacons(scanResult.getTimestampNanos());

        if(result != null && result.length == 23) {
            byte[] protocolBytes = Arrays.copyOfRange(result,2,6);
            ByteBuffer buffer = ByteBuffer.allocate(4);
            buffer.put(protocolBytes);
            int protocolID = buffer.getInt(0);
            if(protocolID == PROTOCOLID){
                try {
                    Beacon beacon = new Beacon(scanResult);
                    putBeacon(beacon);
                }catch (Beacon.BeaconMalformedException e){
                    Log.e(TAG, "onScanResult: Beacon malformed");
                }
            }else Log.i(TAG, "onScanResult: ProtocolID does not match");
        }else
            Log.i(TAG, "onScanResult: No IBeacon Data detected");
    }

    public static ScanFilter getScanFilter(){
        final ScanFilter.Builder builder = new ScanFilter.Builder();
        byte[] protocolBytes = new byte[23];
        ByteBuffer buffer = ByteBuffer.allocate(23);
        buffer.put(new byte[]{0,0});
        buffer.putInt(PROTOCOLID);
        buffer.put(new byte[]{
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

    private synchronized BeaconManager removeOutdatedBeacons(long currentScanNanos){
        List<Short> outdatedBeacon = new ArrayList<>();
        for(Map.Entry<Short, Beacon> beaconEntry : beaconStorage.entrySet()){
            long elapsedTimeNanos = currentScanNanos - beaconEntry.getValue().getTimeNanos();
            if(elapsedTimeNanos > BEACON_ELAPSED_TIME_THRESHHOLD_NANOS){
                outdatedBeacon.add(beaconEntry.getKey());
                Log.d(TAG, "removeOutdatedBeacons: Beacon removed for StationID " + beaconEntry.getKey() + " Elapsedtime: " + elapsedTimeNanos);
            }
        }
        for(Short s : outdatedBeacon){
            beaconStorage.remove(s);
        }
        return this;
    }
    private synchronized BeaconManager putBeacon(Beacon beacon) {

        final Intent intent = new Intent(context, MainService.class);
        intent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_NEW_BEACON);

        if (beaconStorage.containsKey(beacon.getStationID())) {
            beaconStorage.remove(beacon.getStationID());
            beaconStorage.put(beacon.getStationID(),beacon);
            Log.i(TAG, "putBeacon: Updated Beacon " + beacon.print());

        }else{
            beaconStorage.put(beacon.getStationID(),beacon);
            Log.i(TAG, "putBeacon: Put new Beacon " + beacon.print());
        }

        context.startService(intent);
        return this;
    }

    public synchronized Beacon getClosestBeacon(){

        Iterator<Beacon> beaconIterator = beaconStorage.values().iterator();
        Beacon next, nearest = null;
        long time = System.currentTimeMillis();
        try {

            while (beaconIterator.hasNext()) {
                next = beaconIterator.next();
                if ((time-(next.getTimeNanos())) < 1000000){
                    if(nearest == null || nearest.getRssi() < next.getRssi())
                        nearest = next;
                }else {
                    Log.i(TAG, "getClosestBeacon: Beacon too old");
                    beaconStorage.remove(next.getStationID());
                }

            }

        }catch(NoSuchElementException e){

            Log.e(TAG, "getClosestBeacon: no Such Element", e);
            return null;

        }

        Log.i(TAG, "getClosestBeacon: " + nearest.print());
        return nearest;

    }


}
