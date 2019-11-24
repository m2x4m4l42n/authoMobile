package com.mva.authomobile.data;

public class Station {

    private int currentRssi;
    private Beacon currentBeacon;
    private long lastUpdated;

    public Station(Beacon beacon){

    }

    void update(int rssi, Beacon beacon){
        currentBeacon = beacon;
        currentRssi = rssi;
        lastUpdated = System.currentTimeMillis();
    }

}
