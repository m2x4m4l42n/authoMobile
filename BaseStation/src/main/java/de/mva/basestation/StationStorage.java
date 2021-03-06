package de.mva.basestation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

/**
 * Management class for station data
 *
 */
public class StationStorage {
    private final HashMap<Short, Station> stationStorage;
    private Semaphore semaphore;

    public StationStorage(){
        stationStorage = new HashMap<>();
        semaphore = new Semaphore(1);
    }

    boolean addStation(short stationID, SerialConnection serialConnection){
        try {
            semaphore.acquire();
            stationStorage.put(stationID, new Station(stationID, serialConnection));
            semaphore.release();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return true;
    }

    boolean hasStation(short stationID){
        boolean result;
        try {
            semaphore.acquire();
            result = stationStorage.containsKey(stationID);
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
            return false;
        }
        return result;
    }

    public void generateRandomizedSequences(){
        try{
        semaphore.acquire();
        Iterator<Station> iterator = stationStorage.values().iterator();
        Station station; short seq;
        while(iterator.hasNext()){
            station = iterator.next();
            seq = station.currentSequence();
            seq += 1; seq %= 256;
            station.writeRandomizedSequence(seq, RandomizedSequenceGenerator.getInstance().makeBeaconData(station.getStationID(), seq));
        }
        semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public boolean verifyRandomizedSequence(short stationID, byte[] randomizedSequence, short sequenceID){
        try {
            semaphore.acquire();
            final Station station = stationStorage.get(stationID);
            final byte[] rsStation;
            if (station == null)
                System.out.println("No such station in station storage station id: " + stationID);
            else
                System.out.println("Verifiying Station " + stationID + "Sequence " + sequenceID + " Current Sequence " + station.currentSequence());
            if (station != null) {
                if (((station.currentSequence() - sequenceID) % 256) < ApplicationParameters.RANDOMIZED_SEQUENCE_CYCLE_THRESHHOLD) {
                    StringBuilder stringBuilder = new StringBuilder();
                    rsStation = Arrays.copyOfRange(station.getRandomizedSequence(sequenceID), 8, 20);
                    for (byte b : rsStation)
                        stringBuilder.append(String.format("%02X", (b & 0xFF)));
                    stringBuilder.append(" ---- ");
                    for (byte b : randomizedSequence)
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
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }
}
