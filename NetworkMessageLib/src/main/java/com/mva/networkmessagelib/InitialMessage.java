package com.mva.networkmessagelib;

public class InitialMessage extends ConnectionMessage {

    public static final int MSGTYPE = 1;
    private final int userID;
    private final short stationID;
    private final byte[] randomizedSequence;
    private final short sequenceNo;

    public InitialMessage(int userID, short stationID, byte[] randomizedSequence, short sequenceNo){
        this.userID = userID;
        this.sequenceNo = sequenceNo;
        this.stationID = stationID;
        this.randomizedSequence = randomizedSequence;
    }

    @Override
    public int getMessageType() {
        return MSGTYPE;
    }

    public int getUserID(){
        return userID;
    }
    public short getStationID(){
        return stationID;
    }
    public byte[] getRandomizedSequence() {
        return randomizedSequence;
    }

    public short getSequenceNo() {
        return sequenceNo;
    }
}
