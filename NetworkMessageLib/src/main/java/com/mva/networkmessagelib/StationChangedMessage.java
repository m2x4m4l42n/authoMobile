package com.mva.networkmessagelib;

public class StationChangedMessage extends ConnectionMessage {

    public static final int MSGTYPE = 3;

    private final short previousStationID;
    private final short newStationID;

    public StationChangedMessage(short previousStationID, short newStationID){
        this.previousStationID = previousStationID;
        this.newStationID = newStationID;
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

}
