package de.mva.basestation;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomizedSequenceGenerator implements Runnable{

    private static final String TAG = "RandomizedSequenceGenerator";
    private static final long RSG_INTERVAL = 1000;

    private SecureRandom secureRandom;
    private boolean running;
    private Callback callback;

    interface Callback{
        void onNewRandomizedSequence(byte[] randomizedSequence);
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
    }

    @Override
    public void run() {
        byte[] randomizedSequence = new byte[12];
        while(isRunning()){
            secureRandom.nextBytes(randomizedSequence);
            callback.onNewRandomizedSequence(randomizedSequence);
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
