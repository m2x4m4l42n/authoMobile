package com.mva.authomobile.network.message;

public class ConnectionRefusedMessage extends ConnectionMessage {

    public static final int MSG_TYPE = 5;

    @Override
    public int getMessageType() {
        return MSG_TYPE;
    }
}
