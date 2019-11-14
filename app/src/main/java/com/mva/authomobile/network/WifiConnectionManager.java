package com.mva.authomobile.network;

import android.Manifest;
import android.bluetooth.le.ScanFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.util.Pair;

import com.mva.authomobile.R;
import com.mva.authomobile.activity.MainActivity;

import java.util.Iterator;
import java.util.List;

public class WifiConnectionManager {

    private static WifiConnectionManager instance;

    private Context context;

    private boolean connected;

    public static final String PROTOCOLIDENTIFIER = "AuthoMobile1.0";

    private WifiConnectionManager(Context context){
        this.context = context;
    }

    public void disconnect(){
        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
        Iterator<WifiConfiguration> iterator = wifiConfigurationList.iterator();
        WifiConfiguration wifiConfiguration;
        while(iterator.hasNext()){
            wifiConfiguration = iterator.next();
            if(wifiConfiguration.SSID == context.getString(R.string.protocol_ssid)){
                wifiManager.disconnect();
                wifiManager.enableNetwork(wifiConfiguration.networkId, false);
                wifiManager.reconnect();
            }
        }
    }
    public void connect(){

        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if(wifiInfo.getSSID().equals(context.getString(R.string.protocol_ssid))) return;

        WifiConfiguration configuration = new WifiConfiguration();
        configuration.SSID = context.getString(R.string.protocol_ssid);
        configuration.preSharedKey = "\"" + context.getResources().getString(R.string.presharedKey);
        wifiManager.addNetwork(configuration);

        final List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
        Iterator<WifiConfiguration> iterator = wifiConfigurationList.iterator();
        WifiConfiguration wifiConfiguration;
        while(iterator.hasNext()){
            wifiConfiguration = iterator.next();
            if(wifiConfiguration.SSID == context.getString(R.string.protocol_ssid)){
                wifiManager.disconnect();
                wifiManager.enableNetwork(wifiConfiguration.networkId, true);
                wifiManager.reconnect();

            }
        }


    }

    public static WifiConnectionManager getInstance(Context context){
        if(instance == null)
            instance = new WifiConnectionManager(context);
        return instance;
    }

    public boolean isConnected(){
        return connected;
    }



}
