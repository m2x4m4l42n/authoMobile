package de.mva.basestation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Management class for station data
 *
 */
public class StationStorage {
    private final HashMap<Short, Station> stationStorage;

    public StationStorage(){
        stationStorage = new HashMap<>();
    }

    boolean addStation(short stationID, SerialConnection serialConnection){
        stationStorage.put(stationID, new Station(stationID, serialConnection));
        return true;
    }

    boolean hasStation(short stationID){
        return stationStorage.containsKey(stationID);
    }

    public void generateRandomizedSequences(){
        Iterator<Station> iterator = stationStorage.values().iterator();
        Station station; short seq;
        while(iterator.hasNext()){
            station = iterator.next();
            seq = station.currentSequence();
            seq += 1; seq %= 256;
            station.writeRandomizedSequence(seq, RandomizedSequenceGenerator.getInstance().makeBeaconData(station.getStationID(), seq));
        }
    }

    public boolean verifyRandomizedSequence(short stationID, byte[] randomizedSequence, short sequenceID){
        final Station station = stationStorage.get(stationID);
        final byte[] rsStation;
        if(station == null) System.out.println("No such station in station storage station id: "+stationID);
        else System.out.println("Verifiying Station " + stationID + "Sequence " + sequenceID + " Current Sequence " + station.currentSequence());
        if(station != null) {
            if (((station.currentSequence() - sequenceID) % 256) < ApplicationParameters.RANDOMIZED_SEQUENCE_CYCLE_THRESHHOLD) {
                StringBuilder stringBuilder = new StringBuilder();
                rsStation = Arrays.copyOfRange(station.getRandomizedSequence(sequenceID),8,20);
                for(byte b : rsStation)
                    stringBuilder.append(String.format("%02X", (b & 0xFF)));
                stringBuilder.append(" ---- ");
                for(byte b : randomizedSequence)
                    stringBuilder.append(String.format("%02X", (b & 0xFF)));
                System.out.println(stringBuilder.toString());
                if (Arrays.equals(rsStation, randomizedSequence)) {
                    return true;
                } else
                    System.out.println("Randomized Sequence not identical");
            } else
                System.out.println("Sequence too old");
        } else
            System.out.println("No Station found");

        return false;
    }
}
