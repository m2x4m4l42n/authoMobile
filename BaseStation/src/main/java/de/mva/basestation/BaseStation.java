package de.mva.basestation;


import com.mva.networkmessagelib.ConnectedMessage;
import com.mva.networkmessagelib.ConnectionMessage;
import com.mva.networkmessagelib.ConnectionRefusedMessage;
import com.mva.networkmessagelib.ConnectionTerminatedMessage;
import com.mva.networkmessagelib.StationChangedMessage;
import com.mva.networkmessagelib.VerificationMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
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

    private StationStorage stationStorage;
    private HashMap<Integer, Short> userOccupation;
    private HashMap<Integer, OccupationTimeout> userTimeout;

    public static void main(String[] args){
        System.out.println("This runs");
        new Thread(new BaseStation()).start();
    }

    public BaseStation(){

        stationStorage = new StationStorage();
        userOccupation = new HashMap<>();
        userTimeout = new HashMap<>();
        final Enumeration<CommPortIdentifier> comPorts = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier comPort;
        while(comPorts.hasMoreElements()) {
            comPort = comPorts.nextElement();
            for(int i = 0; i< ApplicationParameters.BEACON_PORTS.length ; i++){
                if(comPort.getPortType() == CommPortIdentifier.PORT_SERIAL && ApplicationParameters.BEACON_PORTS[i].equals(comPort.getName())) {
                    stationStorage.addStation(RandomizedSequenceGenerator.getInstance().makeNewStationID(), new SerialConnection(comPort.getName(),ApplicationParameters.BAUDRATE[i]));
                    break;
                }
            }
        }
        new MessageServer(this);

    }

    private ConnectionMessage onVerificationMessageReceived(VerificationMessage message){
        final int userID = message.getUserID();
        final short stationID = message.getStationID();
        if(stationStorage.verifyRandomizedSequence(stationID,message.getRandomizedSequence(),message.getSequenceNo()))
            return  verifyStationOccupation(stationID, userID);
        return new ConnectionTerminatedMessage();
    }
    private synchronized ConnectionMessage verifyStationOccupation(short stationID, int userID){

        if(!stationStorage.hasStation(stationID)) return new ConnectionRefusedMessage();

        ConnectionMessage message = null;

        if(userOccupation.containsKey(userID)){
            final short currentOccupation = userOccupation.get(userID);
            if(currentOccupation != stationID){
                userOccupation.remove(userID);
                Log.print("Station Changed for User " + userID +" from " + currentOccupation + " to " + stationID);
                message = new StationChangedMessage(currentOccupation, stationID);
            }
        }
        if(userOccupation.containsValue(stationID)){
            for(Map.Entry<Integer,Short> entry : userOccupation.entrySet()){
                if(entry.getValue().equals(stationID)){
                    final int currentUser = entry.getKey();
                    if(currentUser != userID){
                        System.out.println("Station " + stationID + " already occupied by " + currentUser);
                        message = new ConnectionRefusedMessage();
                    }
                }
            }
        }
        userOccupation.put(userID,stationID);
        Log.print("Station " + stationID + " occupied by " + userID);
        setTimeout(userID);
        if(message == null) message = new ConnectedMessage(stationID);
        return message;
    }

    void setTimeout(int userID){
        if(userTimeout.containsKey(userID))
            userTimeout.get(userID).reset();
        else
            userTimeout.put(userID,new OccupationTimeout(ApplicationParameters.USER_TIMEOUT , new OccupationTimeout.Callback() {
                @Override
                public void onTimeout() {
                    Log.print("Timeout for userID " + userID + " on Station " + userOccupation.get(userID));
                    userOccupation.remove(userID);
                    userTimeout.remove(userID);
                }
            }));
    }


    @Override
    public Object onMessageReceived(ConnectionMessage message) {
        switch(message.getMessageType()) {
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
                Thread.sleep(ApplicationParameters.SEQUENCE_INTERVAL);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

