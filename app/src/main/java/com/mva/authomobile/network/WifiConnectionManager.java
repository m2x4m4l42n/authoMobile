package com.mva.authomobile.network;

import android.Manifest;
import android.bluetooth.le.ScanFilter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.constraint.solver.widgets.ConstraintAnchor;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Pair;

import com.mva.authomobile.R;
import com.mva.authomobile.activity.MainActivity;
import com.mva.authomobile.service.MainService;

import java.util.Iterator;
import java.util.List;

public class WifiConnectionManager {

    private static WifiConnectionManager instance;

    private static final String TAG = "WifiConnectionManager";
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
            if(wifiConfiguration.SSID.equals(context.getString(R.string.protocol_ssid))){
                wifiManager.disconnect();
                wifiManager.enableNetwork(wifiConfiguration.networkId, false);
                wifiManager.reconnect();
            }
        }
    }
    public void connect(){


        final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final SupplicantState supplicantState = wifiInfo.getSupplicantState();

        Log.d(TAG, "connect: SupplicantState: " + supplicantState.toString());

        if(wifiInfo.getSSID().equals(context.getString(R.string.protocol_ssid)) && supplicantState.equals(SupplicantState.COMPLETED)) {
            Log.d(TAG, "connect: Wifi Connection already established");
            if(!connected){
                final Intent intent = new Intent(context, MainService.class);
                intent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_WIFI_CONNECTED);
                intent.putExtra(WifiConnectionBroadcastReceiver.WIFI_INFO_IDENTIFIER, wifiInfo);
                intent.putExtra(WifiConnectionBroadcastReceiver.DHCP_INFO_IDENTIFIER, wifiManager.getDhcpInfo());
                context.startService(intent);
                connected = true;
            }
            return;
        }else if(supplicantState.equals(SupplicantState.ASSOCIATING)|| supplicantState.equals(SupplicantState.FOUR_WAY_HANDSHAKE)|| supplicantState.equals(SupplicantState.AUTHENTICATING)|| supplicantState.equals(SupplicantState.GROUP_HANDSHAKE)|| supplicantState.equals(SupplicantState.SCANNING) ){
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

    public boolean isConnected(){
        return connected;
    }



}
