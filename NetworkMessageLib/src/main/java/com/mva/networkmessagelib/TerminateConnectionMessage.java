package com.mva.networkmessagelib;

public class TerminateConnectionMessage extends ConnectionMessage {

    public static final int MSGTYPE = 6;

    public short getStationID() {
        return stationID;
    }

    private short stationID;

    public TerminateConnectionMessage(short stationID){
        this.stationID = stationID;
    }

    @Override
    public int getMessageType(){
        return MSGTYPE;
    }

}
