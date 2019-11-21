package com.mva.networkmessagelib;

public class StationChangedMessage extends ConnectionMessage {

    public static final int MSGTYPE = 3;

    short previousStationID;
    short newStationID;
    byte[] randomizedSequence;
    short sequenceID;

    @Override
    public int getMessageType() {
        return MSGTYPE;
    }
}
