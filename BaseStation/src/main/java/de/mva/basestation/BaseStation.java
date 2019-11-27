package de.mva.basestation;


import com.mva.networkmessagelib.ConnectedMessage;
import com.mva.networkmessagelib.ConnectionMessage;
import com.mva.networkmessagelib.ConnectionRefusedMessage;
import com.mva.networkmessagelib.ConnectionTerminatedMessage;
import com.mva.networkmessagelib.InitialMessage;
import com.mva.networkmessagelib.StationChangedMessage;
import com.mva.networkmessagelib.TerminateConnectionMessage;
import com.mva.networkmessagelib.VerificationMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import gnu.io.CommPortIdentifier;


/*
 * RXTX binary builds provided by Mfizz Inc. (http://mfizz.com/).
 *  Please see http://mfizz.com/oss/rxtx-for-java for more info.
 */

/**
 * Main entry point and main class of the base station class
 *
 */
public class BaseStation implements MessageServer.ServerListener, Runnable{

    private List<SerialConnection> serialConnections;
    private StationStorage stationStorage;

    private static final String[] BEACON_PORTS = {"COM5"};
    private static final long SEQUENCE_INTERVAL = 1000;

    public static void main(String[] args){
        System.out.println("This runs");
        new Thread(new BaseStation()).start();
    }

    public BaseStation(){

        stationStorage = new StationStorage();
        serialConnections = new ArrayList<>(10);
        final ArrayList<String> beaconPortList = new ArrayList<>(BEACON_PORTS.length);
        beaconPortList.addAll(Arrays.asList(BEACON_PORTS));
        final Enumeration<CommPortIdentifier> comPorts = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier comPort;
        while(comPorts.hasMoreElements()) {
            comPort = comPorts.nextElement();
            if(comPort.getPortType() == CommPortIdentifier.PORT_SERIAL && beaconPortList.contains(comPort.getName())) {
                stationStorage.addStation(RandomizedSequenceGenerator.getInstance().makeNewStationID(), new SerialConnection((comPort.getName())));

            }
        }
        new MessageServer(this);

    }


    private ConnectionMessage onInitialMessageReceived(InitialMessage message){
        if(stationStorage.occupieStation(message.getStationID()) &&
                stationStorage.verifyRandomizedSequence(message.getStationID(),message.getRandomizedSequence(),message.getSequenceNo())) {
            return new ConnectedMessage();
        }
        return new ConnectionRefusedMessage();
    }
    private ConnectionMessage onVerificationMessageReceived(VerificationMessage message){
        if(stationStorage.verifyRandomizedSequence(message.getStationID(),message.getRandomizedSequence(),message.getSequenceNo())) {
            return new ConnectedMessage();
        }
        return new ConnectionRefusedMessage();
    }
    private ConnectionMessage onStationChangedMessageReceived(StationChangedMessage message){
        if(stationStorage.freeStation(message.getPreviousStationID()) &&
                stationStorage.occupieStation(message.getNewStationID()) &&
                stationStorage.verifyRandomizedSequence(message.getNewStationID(), message.getRandomizedSequence(), message.getSequenceID())){
            return new ConnectedMessage();
        }
        return new ConnectionRefusedMessage();
    }
    private ConnectionMessage onTerminateConenctionMessageReceived(TerminateConnectionMessage message){
        if(stationStorage.freeStation(message.getStationID()))
            return new ConnectionTerminatedMessage();
        else
            return new ConnectionRefusedMessage();
    }

    @Override
    public Object onMessageReceived(ConnectionMessage message) {
        switch(message.getMessageType()) {
            case InitialMessage.MSGTYPE:
                System.out.println("Initial Message Received");
                return onInitialMessageReceived((InitialMessage)message);
            case StationChangedMessage.MSGTYPE:

                System.out.println("StationChangedMessage received");
                return onStationChangedMessageReceived((StationChangedMessage)message);
            case VerificationMessage.MSGTYPE:
                System.out.println("Verification Message Received");
                return onVerificationMessageReceived((VerificationMessage) message);
            default:
                System.out.println("Unknown Message Type Received");
        }
        return null;
    }

    @Override
    public void run() {
        while(true) {
            stationStorage.generateRandomizedSequences();

            try {
                Thread.sleep(SEQUENCE_INTERVAL);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

