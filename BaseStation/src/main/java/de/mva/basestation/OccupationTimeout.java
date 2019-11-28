package de.mva.basestation;

import java.util.concurrent.atomic.AtomicBoolean;

public class OccupationTimeout implements Runnable{

    private final long timeoutPeriod;
    private final Callback callback;
    private final AtomicBoolean cancelled;
    private Thread current;

    interface Callback {
        void onTimeout();
    }
    OccupationTimeout(long timeoutPeriod, Callback callback){
        this.timeoutPeriod = timeoutPeriod;
        this.callback = callback;
        cancelled = new AtomicBoolean(false);
        restart();
    }
    private void restart() {
        current = new Thread(this);
        current.start();
    }
    public void cancel() {
        current.interrupt();
        current = null;
    }
    public void reset(){
        current.interrupt();
        current = null;
        restart();
    }
    @Override
    public void run() {
        try{
            Thread.sleep(timeoutPeriod);
            callback.onTimeout();
        }catch(InterruptedException e){
        }
    }
}
