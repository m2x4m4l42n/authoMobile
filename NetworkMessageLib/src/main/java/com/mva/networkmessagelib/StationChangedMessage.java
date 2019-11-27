package com.mva.networkmessagelib;

public class StationChangedMessage extends ConnectionMessage {

    public static final int MSGTYPE = 3;

    private final short previousStationID;
    private final short newStationID;
    private final int userID;
    private final byte[] randomizedSequence;
    private final short sequenceID;

    public StationChangedMessage(short previousStationID, short newStationID,int userID, byte[] randomizedSequence, short sequenceID){
        this.previousStationID = previousStationID;
        this.newStationID = newStationID;
        this.userID = userID;
        this.randomizedSequence = randomizedSequence;
        this.sequenceID = sequenceID;
    }
    @Override
    public int getMessageType() {
        return MSGTYPE;
    }

    public short getPreviousStationID() {
        return previousStationID;
    }

    public short getNewStationID() {
        return newStationID;
    }

    public int getUserID(){return userID;}

    public byte[] getRandomizedSequence() {
        return randomizedSequence;
    }

    public short getSequenceID() {
        return sequenceID;
    }
}
