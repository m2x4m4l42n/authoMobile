package de.mva.basestation;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

/**
 * main data mangament class for station data
 *
 */
public class Station {

    private final short stationID;
    private final HashMap<Short, byte[]> randomizedSequenceStorage;
    private final SerialConnection serialConnection;
    private short sequenceNo = 0;
    private Semaphore semaphore;


    Station(short stationID, SerialConnection serialConnection){
        this.stationID = stationID;
        this.serialConnection = serialConnection;
        randomizedSequenceStorage = new HashMap<>(256);
        semaphore = new Semaphore(1);
    }

    public void writeRandomizedSequence(short sequenceNo, byte[] randomizedSequence){
        this.sequenceNo = sequenceNo;
        try {
            semaphore.acquire();
            randomizedSequenceStorage.put(sequenceNo,randomizedSequence);
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(byte b : randomizedSequence)
            builder.append(String.format("%02X ", (b & 0xFF)));
        if(serialConnection.isOpen()) {
            serialConnection.write(randomizedSequence);
            System.out.println("Station " + stationID +" wrote " + builder.toString());
        }
    }
    public byte[] getRandomizedSequence(short sequenceNo){
        byte[] result;
        try{
            semaphore.acquire();
            result = randomizedSequenceStorage.get(sequenceNo);
            semaphore.release();
        }catch (InterruptedException e){
            e.printStackTrace();
            return null;
        }
        return result;

    }
    public short currentSequence(){
        return sequenceNo;
    }

    public short getStationID() {
        return stationID;
    }

}
