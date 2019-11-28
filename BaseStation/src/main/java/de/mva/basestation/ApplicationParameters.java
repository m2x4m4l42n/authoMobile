package de.mva.basestation;

public class ApplicationParameters {

    // Serial Connection related parameters:
    static final String[] BEACON_PORTS =    {"COM5"};   // List of all Serial Ports that have a beacon device attached to it
    static final int[] BAUDRATE =           {115200};   // List of all Baudrates specific to the corresponding port

    // Protocol related parameters
    static final int PROTOCOLID = 1431655765;
    static final long SEQUENCE_INTERVAL = 1000;
    static final int RANDOMIZED_SEQUENCE_CYCLE_THRESHHOLD = 4;
    static final int RSSI_THRESHHOLD = -60;
    static final long USER_TIMEOUT = 5000;

    // Network related parameters
    static final int PORT = 8080;



}
