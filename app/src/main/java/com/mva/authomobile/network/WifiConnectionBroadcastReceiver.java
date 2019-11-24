package com.mva.authomobile.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.mva.authomobile.service.MainService;




public class WifiConnectionBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "WifiConnectionBroadcast";


    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        mContext = context;


        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();



            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI &&
                    networkInfo.isConnected()) {
                // Wifi is connected
                WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ssid = wifiInfo.getSSID();
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();


                Log.i(TAG, " -- Wifi connected --- " + " SSID " + ssid + " Gateway-IP " + dhcpInfo.gateway);

                final Intent serviceIntent = new Intent(context, MainService.class);
                serviceIntent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_WIFI_CONNECTED);
                serviceIntent.putExtra(WifiConnectionManager.WIFI_INFO_IDENTIFIER, wifiInfo);
                serviceIntent.putExtra(WifiConnectionManager.DHCP_INFO_IDENTIFIER, dhcpInfo);
                mContext.startService(serviceIntent);

            }
        }
        else if (intent.getAction().equalsIgnoreCase(WifiManager.WIFI_STATE_CHANGED_ACTION))
        {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
            if (wifiState == WifiManager.WIFI_STATE_DISABLED)
            {
                Log.e(TAG, " ----- Wifi  Disconnected ----- ");

                final Intent serviceIntent = new Intent(context, MainService.class);
                serviceIntent.putExtra(MainService.ACTION_IDENTIFIER, MainService.ACTION_WIFI_DISCONNECTED);
                context.startService(serviceIntent);
            }

        }
    }
}

