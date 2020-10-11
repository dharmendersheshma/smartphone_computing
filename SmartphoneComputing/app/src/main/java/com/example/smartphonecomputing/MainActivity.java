package com.example.smartphonecomputing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    WifiManager wifiManager;
    ArrayList<String> wifiList = new ArrayList<>();
    ListView listView;
    List<ScanResult> results;
    WifiReceiver wifiReceiver;
    Button buttonScan;
    String ssid = "MrJio";  // Set ssid name
    int thresold = -40;     // set thresold for ssid to check the current strength of sigal

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permission for location services
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, 1);

        buttonScan = findViewById(R.id.scanBtn);

        //Button click listner for scanning wifi
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });


        listView = findViewById(R.id.myList);  // list which we will polpulate by wifi access points

        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);  // create wifi manager

        wifiReceiver = new WifiReceiver();  // create wifi receiver

        if (!wifiManager.isWifiEnabled()) {   // checking if wifi is enabled or not
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, wifiList);  // create adapter for handling list we create
        listView.setAdapter(adapter);  // set adapter on our created list

        scanWifi();  //start scan
    }

    void scanWifi(){
        wifiList.clear();  //Clear list on each new scan
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));  // Broadcast receiver registeration
        wifiManager.startScan(); //After scan the result is handled by the wifi receiver class(defined below) registered for the Braodcast receiver
    }

    class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();  //scan results

            unregisterReceiver(wifiReceiver);    //free the receiver

            if(results.isEmpty())   //if there is no wifi hotspot in area
            {
                wifiList.add("No Wifi available!");
                adapter.notifyDataSetChanged();
            }
            else
            {
                for (ScanResult scanResult : results) {
                    if(ssid.equals(scanResult.SSID) && thresold < scanResult.level)   //Checking our known ssid defined above as "ssid"
                    {
                        sendNotification(this); //Give notification to user

                        Toast.makeText(getApplicationContext(),"Check notification of your phone!",Toast.LENGTH_SHORT).show();  //Toast
                    }

                    wifiList.add("Wifi SSID: " + scanResult.SSID + "\n" + "Wifi signal strength: " +  scanResult.level + " dBm");  //Add available access points

                    adapter.notifyDataSetChanged();   // Change the list on new scan
                }
            }
        }
    };
    // Class to give notification to user
    public void sendNotification(WifiReceiver view) {

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel("ID", "Name", importance);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(getApplicationContext(), notificationChannel.getId());
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext());
        }

        builder = builder
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText("Hey you are in living room! Do you want to turn TV on?")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        notificationManager.notify(01, builder.build());
    }
}