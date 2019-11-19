package com.mva.authomobile.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.SupportActivity;
import android.support.v4.content.res.TypedArrayUtils;
import android.util.Log;

import com.mva.authomobile.R;
import com.mva.authomobile.activity.MainActivity;
import com.mva.authomobile.application.AuthoMobile;
import com.mva.authomobile.ble.BleManager;
import com.mva.authomobile.ble.BluetoothLEBroadcastReceiver;
import com.mva.authomobile.ble.BluetoothLECallback;
import com.mva.authomobile.data.Beacon;
import com.mva.authomobile.data.BeaconManager;
import com.mva.authomobile.network.WifiConnectionBroadcastReceiver;
import com.mva.authomobile.network.WifiConnectionManager;
import com.mva.authomobile.network.message.ConnectedMessage;
import com.mva.authomobile.network.message.ConnectionMessage;
import com.mva.authomobile.network.message.ConnectionRefusedMessage;
import com.mva.authomobile.network.message.InitialMessage;
import com.mva.authomobile.network.NetworkManager;
import com.mva.authomobile.network.message.StationChangedMessage;
import com.mva.authomobile.network.message.VerificationMessage;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)

public class MainService extends Service {

    private static final String TAG = "MainService";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private static final long SCAN_INTERVAL = 5000;

    public static final String ACTION_IDENTIFIER = "android.authomobile.main_service.action_identifier";
    public static final int ACTION_NEW_BEACON = 1;
    public static final int ACTION_WIFI_CONNECTED = 2;
    public static final int ACTION_WIFI_DISCONNECTED = 3;
    public static final int ACTION_MESSAGE_RECEIVED = 4;
    public static final int ACTION_SCAN_RESULT = 5;

    private boolean running = false;
    private synchronized boolean isRunning(){
        return running;
    }
    private synchronized void setRunning(){
        running = true;
    }

    private boolean wifiConnected;
    private SendBeaconTask sendBeaconTask;

    private class SendBeaconTask implements Runnable{

        private final Handler handler;
        private Beacon beacon;

        SendBeaconTask(Handler handler){
            this.handler = handler;
            handler.post(this);
        }

        @Override
        public void run() {
            beacon = BeaconManager.getInstance(getApplicationContext()).getClosestBeacon();
            if(beacon != null && wifiConnected)
                NetworkManager.getInstance(getApplicationContext()).sendMessage(new VerificationMessage(beacon));

            handler.postDelayed(this, 1000);
        }

        public void stop() {
            handler.removeCallbacks(this);
        }


    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(!isRunning()) start();

        switch(intent.getIntExtra(ACTION_IDENTIFIER, 0)){
            case ACTION_NEW_BEACON:
                onNewBeaconReceived();
                break;
            case ACTION_WIFI_CONNECTED:
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiConnectionBroadcastReceiver.WIFI_INFO_IDENTIFIER);
                DhcpInfo dhcpInfo =intent.getParcelableExtra(WifiConnectionBroadcastReceiver.DHCP_INFO_IDENTIFIER);
                onWifiConnected(wifiInfo,dhcpInfo);
                break;
            case ACTION_WIFI_DISCONNECTED:
                onWifiDisconnected();
                break;
            case ACTION_MESSAGE_RECEIVED:
                onMessageReceived(intent.getIntExtra(NetworkManager.MSG_TYPE, -1),(ConnectionMessage) intent.getParcelableExtra(NetworkManager.MESSAGE_IDENTIFIER));
                break;
            case ACTION_SCAN_RESULT:
                final ScanResult scanResult = intent.getParcelableExtra(BluetoothLEBroadcastReceiver.SCAN_RESULT);
                onScanResult(scanResult);
                break;
            case 0:
                break;
            default:
                Log.i(TAG, "onStartCommand: Action Identifier unkown");
        }

        return super.onStartCommand(intent, flags, startId);

    }

    private void onNewBeaconReceived(){
        if(!wifiConnected)
            WifiConnectionManager.getInstance(getApplicationContext()).connect();
    }
    private void onWifiConnected(WifiInfo wifiInfo, DhcpInfo dhcpInfo){

        if(wifiInfo.getSSID().equals(getString(R.string.protocol_ssid))) {

            byte[] myIPAddress = BigInteger.valueOf(dhcpInfo.gateway).toByteArray();
            ByteBuffer buffer = ByteBuffer.allocate(myIPAddress.length);
            buffer.put(myIPAddress);
            buffer.flip();
            buffer.get(myIPAddress);
            try {
                InetAddress myInetIP = InetAddress.getByAddress(myIPAddress);
                Beacon beacon = BeaconManager.getInstance(getApplicationContext()).getClosestBeacon();
                int userID = 1;
                NetworkManager.getInstance(getApplicationContext()).setRemoteAddress(myInetIP).sendMessage(new InitialMessage(userID,beacon.getStationID(),beacon.getRandomizedSequence(),beacon.getSequenceID()));
                wifiConnected = true;




            } catch (UnknownHostException e) {
                Log.e(TAG, "onWifiConnected: Unkown host exception", e);
            }

        }


    }
    private void onWifiDisconnected(){
        wifiConnected = false;
        if(sendBeaconTask != null) sendBeaconTask.stop();
    }
    private void onMessageReceived(int type, ConnectionMessage message){

        Log.i(TAG, "onMessageReceived: Message Received of Type " + type);
        switch (type){
            case ConnectedMessage
                    .MSG_TYPE:
                sendBeaconTask = new SendBeaconTask(new Handler(getMainLooper()));
            break;
            case ConnectionRefusedMessage
                    .MSG_TYPE:
                Log.e(TAG, "onMessageReceived: Connection Refused");
                WifiConnectionManager.getInstance(getApplicationContext()).disconnect();
                break;
                default:
                    Log.e(TAG, "onMessageReceived: No action defined for Message");
        }

    }
    private void onScanResult(ScanResult scanResult){
        Log.i(TAG, "onScanResult: "+ scanResult.getScanRecord().toString());
        BeaconManager.getInstance(getApplicationContext()).onScanResult(scanResult);
    }

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
        BleManager.getInstance(getApplicationContext()).addFilter(BeaconManager.getScanFilter()).setScanSettings(BleManager.makeDefaultScanSettings()).startPeriodicScan(SCAN_INTERVAL);
        setRunning();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        BleManager.getInstance(getApplicationContext()).stopScan();
    }




}
