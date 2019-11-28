package com.mva.authomobile.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mva.authomobile.data.Beacon;
import com.mva.authomobile.data.BeaconManager;
import com.mva.authomobile.network.NetworkManager;
import com.mva.authomobile.service.MainService;
import com.mva.authomobile.R;
import com.mva.networkmessagelib.InitialMessage;

import org.w3c.dom.Text;

/**
 *  Main Activity which is used to initiate the main service and enable BLE Services
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity{

    public static final String TAG = "MAINACTIVITY";
    public static final String ACTION_NEW_BEACON = "com.authomobile.action.new_beacon";
    public static final int REQUEST_ENABLE_BT = 20;
    public static final int REQUEST_PERMISSION_COARSE_LOCATION = 2;


    private boolean mainServiceStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonService = findViewById(R.id.button_service);
        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mainServiceStarted) startMainService();
                else stopMainService();
            }
        });
        // check system feature & permissions

        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            //TODO: add behaviour in case BLE is not present in phone
            Log.e(TAG, "error phone does not support BLE");
            Toast toast = Toast.makeText(this,"This Phone does not support Bluetooth LE",Toast.LENGTH_LONG);
            toast.show();
        }else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_PERMISSION_COARSE_LOCATION);

        }

        // register receiver for debug data update
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NEW_BEACON);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TextView view = findViewById(R.id.textView);
                TextView stationTextView = findViewById(R.id.stationTextView);
                TextView statusTextView = findViewById(R.id.statusTextView);
                int rssi = intent.getIntExtra("rssi", 0);
                if(rssi != 0) view.setText(String.format("Current RSSI %d",rssi ));
                short station =  intent.getShortExtra("stationID", (short)0);
                if(station != 0) stationTextView.setText(String.format("Station %d",station));
                String status = intent.getStringExtra("status");
                if(status != null) statusTextView.setText(status);
            }
        },filter);
    }
    void stopMainService(){
        stopService(new Intent(this, MainService.class));
        mainServiceStarted = false;
    }
    void startMainService(){
        startService(new Intent(this, MainService.class));
        mainServiceStarted = true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            // your stuff here
            Log.i(TAG, "onActivityResult: REQUEST_ENABLE_BT: RESULT_OK");
            if(!mainServiceStarted) startMainService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_COARSE_LOCATION && permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Intent enableBLEIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBLEIntent, REQUEST_ENABLE_BT);
            Log.i(TAG, "Bluetooth inactive sending Activation Intent");
        }
        else
            Log.i(TAG, "onRequestPermissionsResult: Permissions not granted");
    }

}
