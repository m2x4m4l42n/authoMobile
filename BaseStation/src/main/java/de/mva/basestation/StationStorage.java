package de.mva.basestation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class StationStorage {
    private HashMap<Short, Station> stationStorage;

    public StationStorage(){
        stationStorage = new HashMap<>();
    }
    public Iterator<Station> getStationIterator(){
        return stationStorage.values().iterator();
    }
    public boolean addStation(short stationID, SerialConnection serialConnection){
        stationStorage.put(stationID, new Station(stationID, serialConnection));
        return true;
    }
    public Station getStation(short stationID){
       return stationStorage.get(stationID);
    }
    public boolean occupieStation(short stationID){
        final Station station = stationStorage.get(stationID);
        if(station != null &&
                !station.isOccupied()
        ){
            station.setOccupied(true);
            return true;
        }
        return false;
    }
    public boolean freeStation(short stationID){
        final Station station = stationStorage.get(stationID);
        if(station != null &&
                station.isOccupied()
        ){
            station.setOccupied(false);
            return true;
        }
        return false;
    }
    public void generateRandomizedSequences(){
        Iterator<Station> iterator = stationStorage.values().iterator();
        Station station; short seq = 0;
        while(iterator.hasNext()){
            station = iterator.next();
            seq = station.currentSequence();
            seq += 1; seq %= 256;
            station.writeRandomizedSequence(seq, RandomizedSequenceGenerator.getInstance().makeBeaconData(station.getStationID(), seq));
        }
    }
    public boolean verifyRandomizedSequence(short stationID, byte[] randomizedSequence, short sequenceID){
        final Station station = stationStorage.get(stationID);
        if(station != null &&
                station.isOccupied() &&
                ((station.currentSequence()-sequenceID) % 256)< 4 &&
                Arrays.equals(station.getRandomizedSequence(sequenceID), randomizedSequence)
        )return true;

        return false;
    }
}
