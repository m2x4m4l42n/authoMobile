package com.mva.authomobile.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;


import com.mva.authomobile.R;
import com.mva.authomobile.service.MainService;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;


/*
 * Manager class that implements the singleton pattern and is used to inititate wifi connections
 */
public class WifiConnectionManager {

    private static WifiConnectionManager instance;

    private static final String TAG = "WifiConnectionManager";
    private Context context;

    private boolean connected;

    public static final String PROTOCOLIDENTIFIER = "AuthoMobile1.0";
    public static final String WIFI_INFO_IDENTIFIER = "authomobile.network.wifi_info";
    public static final String DHCP_INFO_IDENTIFIER = "authomobile.network.dhcp_info";

    class WifiConnectionBroadcastReceiver extends BroadcastReceiver{
        private static final String TAG = "WifiConnectionBroadcast";
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ");
            if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
                setConnected(intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false));
            if(isConnected()){
                setRemoteAddress();
            }
        }
    }
    private WifiConnectionManager(Context context){

        this.context = context;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        context.registerReceiver(new WifiConnectionManager.WifiConnectionBroadcastReceiver(), intentFilter);
    }

    public void disconnect(){
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
        Iterator<WifiConfiguration> iterator = wifiConfigurationList.iterator();
        WifiConfiguration wifiConfiguration;
        while(iterator.hasNext()){
            wifiConfiguration = iterator.next();
            Log.d(TAG, "disconnect: " + wifiConfiguration.SSID);
            if(wifiConfiguration.SSID.equals(context.getString(R.string.protocol_ssid))){
                wifiManager.disconnect();
                wifiManager.removeNetwork(wifiConfiguration.networkId);
                wifiManager.saveConfiguration();
                wifiManager.reconnect();
            }
        }
        setConnected(false);
    }
    public void connect(){
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final SupplicantState supplicantState = wifiInfo.getSupplicantState();

        if(wifiInfo.getSSID().equals(context.getString(R.string.protocol_ssid)) && supplicantState.equals(SupplicantState.COMPLETED)) {
            Log.d(TAG, "connect: Wifi Connection already established");
            if(!isConnected()) setRemoteAddress();
            setConnected(true);
            return;

        }else if(supplicantState.equals(SupplicantState.ASSOCIATING)|| supplicantState.equals(SupplicantState.FOUR_WAY_HANDSHAKE)|| supplicantState.equals(SupplicantState.AUTHENTICATING)|| supplicantState.equals(SupplicantState.GROUP_HANDSHAKE)){
            Log.d(TAG, "connect: Wifi busy " + supplicantState.toString());
            return;
        }

        final List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
        final Iterator<WifiConfiguration> iterator = wifiConfigurationList.iterator();
        WifiConfiguration wifiConfiguration;
        while(iterator.hasNext()){
            wifiConfiguration = iterator.next();
            if(wifiConfiguration.SSID.equals(context.getString(R.string.protocol_ssid))){
                Log.d(TAG, "connect: Attempting to connect to WiFi SSID " + wifiConfiguration.SSID);
                if(supplicantState.equals(SupplicantState.ASSOCIATED)) wifiManager.disconnect();
                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                if(wifiManager.reconnect()) Log.d(TAG, "connect: Reconnect successful");
                else Log.d(TAG, "connect: Reconnect unsuccessful");

                return;

            }
        }

        final WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = context.getString(R.string.protocol_ssid);
        configuration.preSharedKey = context.getResources().getString(R.string.presharedKey);
        wifiManager.addNetwork(configuration);
        Log.d(TAG, "connect: Attempting to connect to WiFi SSID " + configuration.SSID);
        if(supplicantState.equals(SupplicantState.ASSOCIATED))wifiManager.disconnect();
        wifiManager.enableNetwork(configuration.networkId, true);
        if(wifiManager.reconnect()) Log.d(TAG, "connect: Reconnect successful");
        else Log.d(TAG, "connect: Reconnect unsuccessful");



    }

    public static WifiConnectionManager getInstance(Context context){
        if(instance == null)
            instance = new WifiConnectionManager(context);
        return instance;
    }

    public synchronized boolean isConnected(){
        return connected;
    }
    public synchronized void setConnected(boolean connected){ this.connected = connected;}

    public void setRemoteAddress(){
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        final byte[] myIPAddress = BigInteger.valueOf(dhcpInfo.gateway).toByteArray();
        try{
            if(myIPAddress.length != 4) throw new UnknownHostException();
            final byte[] host = {myIPAddress[3],myIPAddress[2],myIPAddress[1],myIPAddress[0]};
            InetAddress remoteAddr = InetAddress.getByAddress(host);
            NetworkManager.getInstance(context).setRemoteAddress(remoteAddr);
            final Intent serviceIntent = new Intent(context, MainService.class);
            serviceIntent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_WIFI_CONNECTED);
            context.startService(serviceIntent);

        }catch (UnknownHostException e){

        }
    }


}
