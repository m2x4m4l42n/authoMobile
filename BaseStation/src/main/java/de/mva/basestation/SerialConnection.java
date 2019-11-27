package de.mva.basestation;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.*;

/**
 * Class that handles serial conenctions specifies the baudrate
 *
 */
    public class SerialConnection {

        public static final int BAUDRATE = 115200;

        Enumeration portList;
        CommPortIdentifier portId;
        SerialPort serialPort;
        final String serialPortId;

        boolean open = false;

        public SerialConnection(String serialPortId){
            this.serialPortId = serialPortId;

            portList = CommPortIdentifier.getPortIdentifiers();

            while (portList.hasMoreElements()) {
                portId = (CommPortIdentifier) portList.nextElement();
                if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    if (portId.getName().equals(serialPortId)) {
                        try {
                            serialPort = (SerialPort)
                                    portId.open("BaseStationApp", 2000);
                            System.out.println("Opened Port " + serialPortId);
                            open = true;
                        } catch (PortInUseException e) { e.printStackTrace();open=false;}
                        try {
                            serialPort.setSerialPortParams(BAUDRATE,
                                    SerialPort.DATABITS_8,
                                    SerialPort.STOPBITS_1,
                                    SerialPort.PARITY_NONE);
                        } catch (UnsupportedCommOperationException e) {e.printStackTrace();open = false;}
                    }
                }
            }
        }

        public void write(byte[] message) {
            try{
                OutputStream stream = serialPort.getOutputStream();
                if(stream == null) throw new IOException();
                stream.write(message);
            }catch(IOException e) {e.printStackTrace();}
        }

        public boolean isOpen() {
            return open;
        }
        public InputStream getInputStream() throws IOException {
            return serialPort.getInputStream();
        }
    }


