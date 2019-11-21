package com.mva.networkmessagelib;

public class VerificationMessage extends ConnectionMessage {

    public static final int MSGTYPE = 2;

    short stationID;
    byte[] randomizedSequence;
    short sequenceNo;

    @Override
    public int getMessageType() {
        return MSGTYPE;
    }


    public VerificationMessage(short stationID, byte[] randomizedSequence, short sequenceNo){
        this.stationID = stationID;
        this.randomizedSequence = randomizedSequence;
        this.sequenceNo = sequenceNo;
    }
}
