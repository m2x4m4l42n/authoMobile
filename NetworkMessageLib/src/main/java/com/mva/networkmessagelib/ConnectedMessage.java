package com.mva.networkmessagelib;

public class ConnectedMessage extends ConnectionMessage {

    public static final int MSG_TYPE = 4;
    final short stationID;

    public ConnectedMessage(short stationID){
        this.stationID = stationID;
    }

    public short getStationID() {
        return stationID;
    }

    @Override
    public int getMessageType() {
        return MSG_TYPE;
    }
}
