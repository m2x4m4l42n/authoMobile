package com.mva.networkmessagelib;

public class ConnectionTerminatedMessage extends ConnectionMessage {

    public static final int MSGTYPE = 7;

    @Override
    public int getMessageType() {
        return MSGTYPE;
    }
}
