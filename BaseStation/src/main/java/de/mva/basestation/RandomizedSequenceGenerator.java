package de.mva.basestation;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomizedSequenceGenerator implements Runnable{

    private static final String TAG = "RandomizedSequenceGenerator";
    private static final long RSG_INTERVAL = 1000;
    private static final int PROTOCOLID = 1431655765;

    private SecureRandom secureRandom;
    private boolean running;
    private Callback callback;
    private byte[] stationID = new byte[2];
    private byte[] beaconData = new byte[23];
    ByteBuffer beaconDataBuffer;

    interface Callback{
        void onNewRandomizedSequence(byte[] randomizedSequence, short sequenceID);
    }

    public RandomizedSequenceGenerator(Callback callback){
        this.callback = callback;
        this.running = true;
        try{
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e){
            System.out.println(TAG + " No Secure Instance" );
            secureRandom = new SecureRandom();
        }
        secureRandom.nextBytes(stationID);
        beaconDataBuffer = ByteBuffer.allocate(23);
    }

    @Override
    public void run() {
        byte[] randomizedSequence = new byte[12];

        short sequenceID = 0;
        while(isRunning()){
            sequenceID +=1; sequenceID %=256;
            secureRandom.nextBytes(randomizedSequence);
            beaconDataBuffer.putInt(PROTOCOLID);
            beaconDataBuffer.put(stationID);
            beaconDataBuffer.put(randomizedSequence);
            beaconDataBuffer.putShort((short)0);
            beaconDataBuffer.put((byte)0);
            beaconDataBuffer.putShort(sequenceID);
            beaconDataBuffer.flip();
            beaconDataBuffer.get(beaconData);
            beaconDataBuffer.clear();
            callback.onNewRandomizedSequence(beaconData, sequenceID);
            try {
                Thread.sleep(RSG_INTERVAL);
            }catch (InterruptedException e){
                System.out.println(TAG + " InteruptedException: " + e.getMessage());
                return;
            }
        }

    }

    private synchronized boolean isRunning(){
        return running;
    }
    private synchronized void stop(){
        running = false;
    }
}
