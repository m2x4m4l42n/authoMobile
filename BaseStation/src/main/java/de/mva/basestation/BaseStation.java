package de.mva.basestation;


import com.mva.networkmessagelib.ConnectedMessage;
import com.mva.networkmessagelib.ConnectionMessage;
import com.mva.networkmessagelib.InitialMessage;
import com.mva.networkmessagelib.StationChangedMessage;
import com.mva.networkmessagelib.VerificationMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import gnu.io.CommPortIdentifier;


/*
 * RXTX binary builds provided by Mfizz Inc. (http://mfizz.com/).
 *  Please see http://mfizz.com/oss/rxtx-for-java for more info.
 */

public class BaseStation implements RandomizedSequenceGenerator.Callback, MessageServer.ServerListener{

    private HashMap<Short,byte[]> randomizedSequenceStorage;
    List<SerialConnection> serialConnections;

    public static void main(String[] args){
        System.out.println("This runs");
        System.out.println(System.getProperty("java.library.path"));
        new Thread(new RandomizedSequenceGenerator(new BaseStation())).start();

    }

    public BaseStation(){

        randomizedSequenceStorage = new HashMap<>(256);
        serialConnections = new ArrayList<>(10);
        final Enumeration<CommPortIdentifier> comPorts = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier comPort;
        while(comPorts.hasMoreElements()) {
            comPort = comPorts.nextElement();
            if(comPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                serialConnections.add(new SerialConnection(comPort.getName()));
            }
        }
        new MessageServer(this);

    }
    @Override
    public void onNewRandomizedSequence(byte[] randomizedSequence, short sequenceID) {
        StringBuilder builder = new StringBuilder();
        for(byte b : randomizedSequence)
            builder.append(String.format("%02X ", (b & 0xFF)));

        System.out.println("Time: " + System.currentTimeMillis() +" RS:  " + builder.toString());

        putRandomizedSequence(randomizedSequence, sequenceID);
        Iterator<SerialConnection> it = serialConnections.iterator();
        SerialConnection serialConnection;
        while(it.hasNext()) {
            serialConnection = it.next();
            if(serialConnection.isOpen()) {
                serialConnection.write(randomizedSequence);
                System.out.println("written");
            }
        }
    }

    private synchronized void putRandomizedSequence(byte[] randomizedSequence, short sequenceID){
        randomizedSequenceStorage.put(sequenceID,randomizedSequence);
    }
    private synchronized byte[] getRandomizedSequence(short sequenceID) {
        return randomizedSequenceStorage.get(sequenceID);
    }

    @Override
    public Object onMessageReceived(ConnectionMessage message) {
        switch(message.getMessageType()) {
            case InitialMessage.MSGTYPE:
                return new ConnectedMessage();
            case StationChangedMessage.MSGTYPE:
                return new ConnectedMessage();
            case VerificationMessage.MSGTYPE:
                return new ConnectedMessage();
        }
        return null;
    }
}

