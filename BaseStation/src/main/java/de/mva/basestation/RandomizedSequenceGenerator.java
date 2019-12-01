package de.mva.basestation;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Class that generates the Randomized sequences with a secure random object
 */
public class RandomizedSequenceGenerator{

    private static final String TAG = "RandomizedSequenceGenerator";


    private SecureRandom secureRandom;
    private ByteBuffer beaconDataBuffer;

    private static RandomizedSequenceGenerator instance;

    public static RandomizedSequenceGenerator getInstance(){
        if(instance == null)
            instance = new RandomizedSequenceGenerator();
        return instance;
    }

    private RandomizedSequenceGenerator(){
        try{
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e){
            System.out.println(TAG + " No Secure Instance" );
            secureRandom = new SecureRandom();
        }
        beaconDataBuffer = ByteBuffer.allocate(23);
    }

    public byte[] makeBeaconData(short stationID, short sequenceNo) {
        byte[] randomizedSequence = new byte[12];
        byte[] beaconData = new byte[23];
        secureRandom.nextBytes(randomizedSequence);
        beaconDataBuffer.clear();
            beaconDataBuffer.put(new byte[]{0,0});
            beaconDataBuffer.putInt(ApplicationParameters.PROTOCOLID);
            beaconDataBuffer.putShort(stationID);
            beaconDataBuffer.put(randomizedSequence);
            beaconDataBuffer.putShort(sequenceNo);
            beaconDataBuffer.put((byte)0);
            beaconDataBuffer.flip();
            beaconDataBuffer.get(beaconData);
            return beaconData;
    }
    public short makeNewStationID(){
        byte[] idBytes = new byte[2];
        secureRandom.nextBytes(idBytes);
        beaconDataBuffer.clear();
        beaconDataBuffer.put(idBytes);
        return beaconDataBuffer.getShort(0);
    }
}
