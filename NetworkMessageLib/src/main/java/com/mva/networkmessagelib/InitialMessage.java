package com.mva.networkmessagelib;

public class InitialMessage extends ConnectionMessage {

    public static final int MSGTYPE = 1;
    int userID;
    short stationID;
    byte[] randomizedSequence;
    short sequenceNo;

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
}
