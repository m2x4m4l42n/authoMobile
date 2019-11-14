package com.mva.authomobile.data;

import android.annotation.TargetApi;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Beacon {
    private static final String TAG = "Beacon";

    private short stationID;
    private byte[] randomizedSequence;
    private short sequenceID;
    private int rssi;
    private long timeNanos;

    public Beacon(ScanResult scanResult) throws BeaconMalformedException{

        final int rssi = scanResult.getRssi();
        final long nanos = scanResult.getTimestampNanos();
        final byte[] ibeacon = scanResult.getScanRecord().getManufacturerSpecificData(BeaconManager.MANUFACTURERID);

        if(ibeacon == null || ibeacon.length != 23) throw new BeaconMalformedException();

        ByteBuffer beaconBuffer = ByteBuffer.allocate(ibeacon.length);
        beaconBuffer.put(ibeacon);
        beaconBuffer.flip();

        setRssi(rssi);
        setSequenceID(beaconBuffer.getShort(7));
        setRandomizedSequence(Arrays.copyOfRange(ibeacon,9,21));
        setStationID(beaconBuffer.getShort(21));
        setTimeNanos(nanos);

    }

    private Beacon setStationID(short stationID){
        this.stationID = stationID;
        return this;
    }

    public Short getStationID(){
        return stationID;
    }
    private Beacon setRandomizedSequence(byte[] randomizedSequence){
        if(randomizedSequence.length == 12){
            this.randomizedSequence = randomizedSequence;
        }else{
            Log.e(TAG, "setRandomizedSequence: False Byte Data");
        }
        return this;
    }

    public byte[] getRandomizedSequence() {
        return randomizedSequence;
    }

    private Beacon setSequenceID(short SequenceID){
        this.sequenceID = sequenceID;
        return this;
    }

    public short getSequenceID() {
        return sequenceID;
    }

    private Beacon setRssi(int rssi){
        this.rssi = rssi;
        return this;
    }

    public int getRssi() {
        return rssi;
    }

    private Beacon setTimeNanos(long nanos){
        timeNanos = nanos;
        return this;
    }

    public long getTimeNanos() {
        return timeNanos;
    }


    public class BeaconMalformedException extends Exception {}

    public String print(){
        StringBuilder builder = new StringBuilder();
        builder.append("StationID " + stationID).append("| Rssi " + rssi).append(" | Time " + timeNanos);
        return builder.toString();
    }
}
