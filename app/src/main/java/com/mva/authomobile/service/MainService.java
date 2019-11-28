package com.mva.authomobile.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mva.authomobile.R;
import com.mva.authomobile.activity.MainActivity;
import com.mva.authomobile.application.AuthoMobile;
import com.mva.authomobile.ble.BleManager;
import com.mva.authomobile.ble.BluetoothLEBroadcastReceiver;
import com.mva.authomobile.data.Beacon;
import com.mva.authomobile.data.BeaconManager;
import com.mva.authomobile.network.NetworkManager;
import com.mva.authomobile.network.WifiConnectionManager;

import com.mva.networkmessagelib.*;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)


/**
 * Core class of the application that is the main entry point and callback for the single components of the application
 * components communicate using the startService method and transfer relevant data as intent extras
 *
 */
public class MainService extends Service {

    private static final String TAG = "MainService";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private static final long SCAN_INTERVAL = 12000;

    public static final String ACTION_IDENTIFIER = "android.authomobile.main_service.action_identifier";
    public static final int ACTION_NEW_BEACON = 1;
    public static final int ACTION_WIFI_CONNECTED = 2;
    public static final int ACTION_WIFI_DISCONNECTED = 3;
    public static final int ACTION_MESSAGE_RECEIVED = 4;
    public static final int ACTION_SCAN_RESULT = 5;
    public static final int ACTION_BEACON_TIMEOUT = 6;


    private boolean running = false;
    private synchronized boolean isRunning(){
        return running;
    }
    private synchronized void setRunning(){
        running = true;
    }

    private final int userID = 12;
    private Beacon station;
    private boolean connected = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Override of the onStartCommand method is used as a callback to the main service from various points in the application
     * relevant data is transferred via the intent extras
     * @param intent object holding relevant data
     * @param flags
     * @param startId
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!isRunning()) start();
        switch(intent.getIntExtra(ACTION_IDENTIFIER, 0)){
            case ACTION_NEW_BEACON:
                Log.d(TAG, "onStartCommand: Start Intent received ACTION_NEW_BEACON");
                onNewBeaconReceived();
                break;
            case ACTION_WIFI_CONNECTED:
                Log.d(TAG, "onStartCommand: Start Intent received ACTION WIFI CONNECTED");
                onWifiConnected();
                break;
            case ACTION_WIFI_DISCONNECTED:
                Log.d(TAG, "onStartCommand: Start Intent received ACTION WIFI DISCONNECTED");
                onWifiDisconnected();
                break;
            case ACTION_MESSAGE_RECEIVED:
                Log.d(TAG, "onStartCommand: Start Intent received ACTION MESSAGE RECEIVED");
                onMessageReceived(intent.getIntExtra(NetworkManager.MSG_TYPE, -1),(ConnectionMessage) intent.getSerializableExtra(NetworkManager.MESSAGE_IDENTIFIER));
                break;
            case ACTION_SCAN_RESULT:
                Log.d(TAG, "onStartCommand: Start Intent received ACTION SCAN RESULT");
                final ScanResult scanResult = intent.getParcelableExtra(BluetoothLEBroadcastReceiver.SCAN_RESULT);
                onScanResult(scanResult);
                break;
            case ACTION_BEACON_TIMEOUT:
                Log.d(TAG, "onStartCommand: Start Intent received ACTION BEACON TIMEOUT");
                onBeaconTimeout();
            case 0:
                break;
            default:
                Log.i(TAG, "onStartCommand: Action Identifier unkown");
        }

        return super.onStartCommand(intent, flags, startId);

    }

    /**
     *  Method that is called when a beacon is detected through the ble scan, if the system is inactive the system transitions to active station
     *  and tries to initate a wifi connection, in active state it sends the corrisponding message to the base station via the network manager
     */
    private void onNewBeaconReceived(){
        Beacon beacon = BeaconManager.getInstance(getApplicationContext()).getClosestBeacon();
        if(beacon!= null){
            final Intent activityIntent = new Intent();
            activityIntent.setAction(MainActivity.ACTION_NEW_BEACON);
            activityIntent.putExtra("rssi", beacon.getRssi());
            sendBroadcast(activityIntent);
            WifiConnectionManager.getInstance(getApplicationContext()).connect();
            NetworkManager.getInstance(getApplicationContext()).sendMessage(new VerificationMessage(userID, beacon.getStationID(),beacon.getRandomizedSequence(), beacon.getSequenceID(), beacon.getRssi()));
        }
    }
    private void onBeaconTimeout(){
        final Intent activityIntent = new Intent();
        activityIntent.setAction(MainActivity.ACTION_NEW_BEACON);
        activityIntent.putExtra("status", "disconnected");
        sendBroadcast(activityIntent);
        WifiConnectionManager.getInstance(getApplicationContext()).disconnect();
    }
    private void onWifiConnected(){

    }
    private void onWifiDisconnected(){

    }

    /**
     * Network manager callback of received response message from the base station, the corrisponding action is performed
     * @param type
     */
    private void onMessageReceived(int type, ConnectionMessage message){

        Log.i(TAG, "onMessageReceived: Message Received of Type " + type);
        final Intent activityIntent = new Intent();
        switch (type){
            case ConnectedMessage.MSG_TYPE:
                Log.i(TAG, "onMessageReceived: Connected");
                connected = true;
                ConnectedMessage msg =(ConnectedMessage) message;
                activityIntent.setAction(MainActivity.ACTION_NEW_BEACON);
                activityIntent.putExtra("status", "Connected");
                activityIntent.putExtra("stationID", msg.getStationID());
                sendBroadcast(activityIntent);
                break;
            case ConnectionRefusedMessage.MSG_TYPE:
                Log.e(TAG, "onMessageReceived: Connection Refused");
                connected = false;
                activityIntent.setAction(MainActivity.ACTION_NEW_BEACON);
                activityIntent.putExtra("status", "Connection Refused");
                sendBroadcast(activityIntent);
                break;
            case StationChangedMessage.MSGTYPE:
                Log.d(TAG, "onMessageReceived: StationChanged");
                activityIntent.setAction(MainActivity.ACTION_NEW_BEACON);
                activityIntent.putExtra("status", "StationChanged");
                sendBroadcast(activityIntent);

                default:
                    Log.e(TAG, "onMessageReceived: No action defined for Message");
                    activityIntent.setAction(MainActivity.ACTION_NEW_BEACON);
                    activityIntent.putExtra("status", "ERROR");
                    sendBroadcast(activityIntent);
        }

    }

    /**
     * callback of the BLE Manager on a received scan result, invokes the BeaconManagers on scanresult method to handle scan result
     * @param scanResult
     */
    private void onScanResult(ScanResult scanResult){
        Log.i(TAG, "onScanResult: "+ scanResult.getScanRecord().toString());
        BeaconManager.getInstance(getApplicationContext()).onScanResult(scanResult);
    }

    /**
     *
     * Intial method that is called on startup and inititates the notification
     */
    private void start(){

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                    new NotificationCompat.Builder(this, AuthoMobile.CHANNEL_ID)
                            .setContentTitle(getText(R.string.notification_title))
                            .setContentText(getText(R.string.notification_message))
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentIntent(pendingIntent)
                            .setTicker(getText(R.string.ticker_text))
                            .build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        BleManager.getInstance(getApplicationContext()).addFilter(BeaconManager.getScanFilter()).setScanSettings(BleManager.makeDefaultScanSettings()).startPeriodicScanWithTimeout(SCAN_INTERVAL);
        setRunning();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BleManager.getInstance(getApplicationContext()).stopScan();
    }




}
