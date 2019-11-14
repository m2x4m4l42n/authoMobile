package com.mva.authomobile.network.message;

import com.mva.authomobile.data.Beacon;


public class VerificationMessage extends ConnectionMessage {

    public static final int MSGTYPE = 2;

    short stationID;
    byte[] randomizedSequence;
    short sequenceNo;

    @Override
    public int getMessageType() {
        return MSGTYPE;
    }


    public VerificationMessage(Beacon beacon){
        this.stationID = beacon.getStationID();
        this.randomizedSequence = beacon.getRandomizedSequence();
        this.sequenceNo = beacon.getSequenceID();
    }
}
