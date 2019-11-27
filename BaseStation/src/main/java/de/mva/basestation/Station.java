package de.mva.basestation;

import java.util.HashMap;

/**
 * main data mangament class for station data
 *
 */
public class Station {

    private short stationID;
    private HashMap<Short, byte[]> randomizedSequenceStorage;
    private boolean occupied = false;
    private short sequenceNo = 0;
    private SerialConnection serialConnection;

    Station(short stationID, SerialConnection serialConnection){
        this.stationID = stationID;
        this.serialConnection = serialConnection;
        randomizedSequenceStorage = new HashMap<>(256);
    }

    public void writeRandomizedSequence(short sequenceNo, byte[] randomizedSequence){
        this.sequenceNo = sequenceNo;
        randomizedSequenceStorage.put(sequenceNo,randomizedSequence);
        StringBuilder builder = new StringBuilder();
        for(byte b : randomizedSequence)
            builder.append(String.format("%02X ", (b & 0xFF)));
        if(serialConnection.isOpen()) {
            serialConnection.write(randomizedSequence);
            System.out.println("Station " + stationID +" wrote " + builder.toString());
        }
    }
    public byte[] getRandomizedSequence(short sequenceNo){
        return randomizedSequenceStorage.get(sequenceNo);
    }

    public boolean isOccupied(){
        return occupied;
    }
    public void setOccupied(boolean status){
        occupied = status;
    }
    public short currentSequence(){
        return sequenceNo;
    }

    public short getStationID() {
        return stationID;
    }
}
