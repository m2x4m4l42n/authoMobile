package com.mva.networkmessagelib;

public class ConnectedMessage extends ConnectionMessage {

    public static final int MSG_TYPE = 4;

    @Override
    public int getMessageType() {
        return MSG_TYPE;
    }
}