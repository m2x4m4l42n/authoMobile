package de.mva.basestation;


import java.util.ArrayList;
import java.util.List;

public class BaseStation implements RandomizedSequenceGenerator.Callback{

    private List<byte[]> randomizedSequenceStorage;

    public static void main(String[] args){
        System.out.println("This runs");

        new Thread(new RandomizedSequenceGenerator(new BaseStation())).start();
    }

    public BaseStation(){

        randomizedSequenceStorage = new ArrayList<>(256);

    }
    @Override
    public void onNewRandomizedSequence(byte[] randomizedSequence) {
        StringBuilder builder = new StringBuilder();
        for(byte b : randomizedSequence)
            builder.append((char) b);

        System.out.println("Time: " + System.currentTimeMillis() +" RS:  " + builder.toString());

        putRandomizedSequence(randomizedSequence);

    }

    private synchronized void putRandomizedSequence(byte[] randomizedSequence){
        randomizedSequenceStorage.add(0,randomizedSequence);
    }
}
