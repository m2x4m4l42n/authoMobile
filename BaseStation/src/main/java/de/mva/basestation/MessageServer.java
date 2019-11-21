package de.mva.basestation;

import com.mva.networkmessagelib.ConnectionMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;



public class MessageServer implements Runnable{

    private boolean running = true;
    private ServerListener listener;
    private static final int PORT = 8080;


    public MessageServer(ServerListener listener){
        this.listener = listener;
        new Thread(this).start();
    }

    public synchronized boolean isRunning(){
        return running;
    }
    public synchronized void stop(){
        running = false;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket clientSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            while(isRunning()) {
                try {
                    clientSocket = serverSocket.accept();
                    try (ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream())) {

                        ConnectionMessage message = (ConnectionMessage) is.readObject();

                        Object response = listener.onMessageReceived(message);
                        if(response != null)
                            os.writeObject(response);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface ServerListener{
        Object onMessageReceived(ConnectionMessage message);
    }
}
