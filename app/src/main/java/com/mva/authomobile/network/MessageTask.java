package com.mva.authomobile.network;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.mva.authomobile.service.MainService;
import com.mva.networkmessagelib.ConnectionMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)

/**
 * AsyncTask extention that is used to send messages over the wifi network to the base station
 */
public class MessageTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = "MessageTask";
    private Object message;
    private Context context;

    public MessageTask(Context context, Object message){
        this.context = context;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {

        if(NetworkManager.getInstance(context.getApplicationContext()).isReady()) {
            try (Socket client = new Socket(NetworkManager.getInstance(context.getApplicationContext()).getRemoteAddress(), NetworkManager.getInstance(context.getApplicationContext()).getTargetPort());
                 ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());
                 ObjectInputStream is = new ObjectInputStream(client.getInputStream())
            ) {
                os.writeObject(message);
                os.flush();

                Object obj = is.readObject();

                if(!(obj instanceof ConnectionMessage))
                    Log.e(TAG, "doInBackground: Object not instance of ConnectionMessage" + obj.toString());
                else {
                    ConnectionMessage response = (ConnectionMessage) obj;
                    final Intent serviceIntent = new Intent(context, MainService.class);
                    serviceIntent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_MESSAGE_RECEIVED);
                    serviceIntent.putExtra(NetworkManager.MSG_TYPE, response.getMessageType());
                    context.startService(serviceIntent);
                }
            } catch (IOException e) {
                Log.e(TAG, "IOException");
            } catch (ClassNotFoundException e){
                Log.e(TAG, "Class not found");
            }
        }

        return null;

    }
}