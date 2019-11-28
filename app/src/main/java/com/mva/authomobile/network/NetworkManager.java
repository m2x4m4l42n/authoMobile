package com.mva.authomobile.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.mva.authomobile.BuildConfig;
import com.mva.networkmessagelib.ConnectionMessage;

import java.io.IOException;
import java.net.InetAddress;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


@TargetApi(Build.VERSION_CODES.KITKAT)

/**
 *  Singleton class that manages network connectivity and sending and receiving messages over the network
 */
public class NetworkManager {

    public static final String MSG_TYPE = "authomobile.network.msg_type";
    public static final String MESSAGE_IDENTIFIER = "authmobile.network.msg_identifier";

    private static final String TAG = "NetworkManager";

    private static NetworkManager instance;

    private InetAddress remoteAddress;
    private int targetPort = 8080;
    private SocketFactory sslSocketFactory;
    private Context context;

    public static NetworkManager getInstance(Context context){
        if(instance == null)
            instance = new NetworkManager();
        instance.context = context;
        return instance;
    }
    private NetworkManager(){
        sslSocketFactory = SSLSocketFactory.getDefault();
    }

    public NetworkManager setRemoteAddress(InetAddress address){
        if(BuildConfig.DEBUG) Log.d(TAG, "setRemoteAddress: "+ address.getHostAddress());
        remoteAddress = address;
        return this;
    }
    public InetAddress getRemoteAddress(){
        return remoteAddress;
    }
    public NetworkManager setTargetPort(int port){
        if(BuildConfig.DEBUG) Log.d(TAG, "set TargetPort " + port);
        targetPort = port;
        return this;
    }
    public int getTargetPort(){
        return targetPort;
    }

    public boolean isReady(){
        if(remoteAddress != null)
            return true;
        Log.e(TAG, "NetworkManager not fully initialized");
        return false;
    }

    public SSLSocket getSSLSocket() throws IOException {
        return (SSLSocket) sslSocketFactory.createSocket(remoteAddress,targetPort);
    }

    public void sendMessage(ConnectionMessage message){
        if(WifiConnectionManager.getInstance(context).isConnected())
            new MessageTask(context,message).execute();
        else{
            WifiConnectionManager.getInstance(context.getApplicationContext()).connect();
            Log.d(TAG, "sendMessage: No Wifi Connection");
        }
    }

}
