package com.mva.networkmessagelib;

public class VerificationMessage extends ConnectionMessage {

    public static final int MSGTYPE = 2;

    private final int userID;
    private final short stationID;
    private final byte[] randomizedSequence;
    private final short sequenceNo;
    private final int rssi;

    @Override
    public int getMessageType() {
        return MSGTYPE;
    }


    public VerificationMessage(int userID, short stationID, byte[] randomizedSequence, short sequenceNo, int rssi){
        this.userID = userID;
        this.stationID = stationID;
        this.randomizedSequence = randomizedSequence;
        this.sequenceNo = sequenceNo;
        this.rssi = rssi;
    }

    public int getUserID(){ return userID; }
    public short getStationID() {
        return stationID;
    }

    public byte[] getRandomizedSequence() {
        return randomizedSequence;
    }

    public short getSequenceNo() {
        return sequenceNo;
    }

    public int getRssi(){
        return rssi;
    }
}
