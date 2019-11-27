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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private HashMap<Integer, Short> userOccupation;

    private static final String[] BEACON_PORTS = {"COM5"};
    private static final long SEQUENCE_INTERVAL = 1000;

    public static void main(String[] args){
        System.out.println("This runs");
        new Thread(new BaseStation()).start();
    }

    public BaseStation(){

        stationStorage = new StationStorage();
        serialConnections = new ArrayList<>(10);
        userOccupation = new HashMap<>();
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

    private ConnectionMessage onVerificationMessageReceived(VerificationMessage message){
        final int userID = message.getUserID();
        final short stationID = message.getStationID();
        if( verifyStationOccupation(stationID, userID) &&
            stationStorage.verifyRandomizedSequence(stationID,message.getRandomizedSequence(),message.getSequenceNo()))
            return new ConnectedMessage();
        return new ConnectionTerminatedMessage();
    }
    private boolean verifyStationOccupation(short stationID, int userID){
        if(!stationStorage.hasStation(stationID))
            return false;
        if(userOccupation.containsKey(userID)){
            final short currentOccupation = userOccupation.get(userID);
            if(currentOccupation != stationID){
                userOccupation.remove(userID);
                System.out.println("Station Changed for User " + userID +" from " + currentOccupation + " to " + stationID);
            }
        }
        if(userOccupation.containsValue(stationID)){
            for(Map.Entry<Integer,Short> entry : userOccupation.entrySet()){
                if(entry.getValue().equals(stationID)){
                    final int currentUser = entry.getKey();
                    if(currentUser != userID){
                        userOccupation.remove(currentUser);
                        System.out.println("Occupation changed for Station " + stationID + " from " + currentUser + " to " + userID);
                    }
                }
            }
        }
        userOccupation.put(userID,stationID);
        return true;
    }

    private ConnectionMessage onInitialMessageReceived(InitialMessage message){
        return new ConnectionRefusedMessage();
    }
    private ConnectionMessage onStationChangedMessageReceived(StationChangedMessage message){
        return new ConnectionRefusedMessage();
    }
    private ConnectionMessage onTerminateConenctionMessageReceived(TerminateConnectionMessage message){
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

