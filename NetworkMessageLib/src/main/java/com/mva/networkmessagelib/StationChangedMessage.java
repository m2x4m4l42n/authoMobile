package com.mva.networkmessagelib;

public class StationChangedMessage extends ConnectionMessage {

    public static final int MSGTYPE = 3;

    private short previousStationID;
    private short newStationID;
    private byte[] randomizedSequence;
    private short sequenceID;

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

    public byte[] getRandomizedSequence() {
        return randomizedSequence;
    }

    public short getSequenceID() {
        return sequenceID;
    }
}
