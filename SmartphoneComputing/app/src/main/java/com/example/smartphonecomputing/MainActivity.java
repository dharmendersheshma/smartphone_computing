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
import android.os.Handler;
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
    Button buttonSelectWifi;
    Button buttonInfo;

    static String ssid1 = "Give ssid 1"; //Set ssid 1
    static String ssid2 = "Give ssid 2"; //Set ssid 2
    static String ssid3 = "Give ssid 3"; //Set ssid 3

    static int threshold1 = 1; //Set signal threshold for ssid 1
    static int threshold2 = 1;  //Set signal threshold for ssid 2
    static int threshold3 = 1;  //Set signal threshold for ssid 3

    //Current signal strength for above three ssids
    int sig1;
    int sig2;
    int sig3;

    static int error = 5; //error range for signal (threshold-5 to threshold+5)

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permission for location services
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, 1);

        buttonScan = findViewById(R.id.scanBtn);
        buttonSelectWifi = findViewById(R.id.user_selection);
        buttonInfo = findViewById(R.id.saved_info);

        buttonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "SSID1: " + ssid1 + "strength: " + threshold1 + "dBm" + "\n" +
                        "SSID2: " + ssid2 + "strength: " + threshold2 + "dBm" + "\n" +
                        "SSID3: " + ssid3 + "strength: " + threshold3 + "dBm" + "\n" +
                        "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });

        //Button click listner for scanning wifi
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //auto scan with 5 second sleep time
                startRepeating();
            }
        });

        //Button click listner for selecting three wifi points
        buttonSelectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewActivity();
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

    public void openNewActivity(){
        Intent intent = new Intent(this, NewActivity.class);
        startActivity(intent);
    }

    public void startRepeating() {
        //mHandler.postDelayed(mToastRunnable, 5000);
        auto_scan.run();
    }
    public void stopRepeating(View view) {
        handler.removeCallbacks(auto_scan);
    }
    private Runnable auto_scan = new Runnable() {
        @Override
        public void run() {
            scanWifi();  //start scan
            Toast.makeText(MainActivity.this, "Scanning..", Toast.LENGTH_SHORT).show();
            handler.postDelayed(this, 5000);
        }
    };

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
                int count = 0; // to check all three ssids which we require are available
                for (ScanResult scanResult : results) {

                    if(ssid1.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid1"
                    {
                        sig1 = scanResult.level;   // set current signal strength of ssid1
                        count++;
                    }
                    else if(ssid2.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid2"
                    {
                        sig2 = scanResult.level;  // set current signal strength of ssid2
                        count++;
                    }
                    else if(ssid3.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid3"
                    {
                        sig3 = scanResult.level;  // set current signal strength of ssid3
                        count++;
                    }
                    if(count==3)  // if all three required wifi access points are available
                    {
                        boolean check = check_location(threshold1, threshold2, threshold3, sig1, sig2, sig3); // check if we are at required location
                        if(check)
                        {
                            sendNotification(this); //Give notification to user

                            Toast.makeText(getApplicationContext(),"Trilocation idetification!",Toast.LENGTH_SHORT).show();  //Toast
                        }
                    }
                    wifiList.add("Wifi SSID: " + scanResult.SSID + "\n" + "Wifi signal strength: " +  scanResult.level + " dBm");  //Add available access points

                    adapter.notifyDataSetChanged();   // Change the list on new scan
                }
            }
        }
    };
    // Function to give notification to user
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
                .setContentText("Trilateration notifiction....Hey you are in living room! Do you want to turn TV on?")
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        notificationManager.notify(01, builder.build());
    }

    //Trilateration function
    public boolean check_location(int threshold1, int threshold2, int threshold3, int sig1, int sig2, int sig3){
        int flag1 = 0, flag2 = 0, flag3 = 0;
        if((sig1 < (threshold1 + error)) && (sig1 > (threshold1 - error)))
        {
            flag1 = 1; // set flag if ssid1 have strength within range of error of 5 dBm plus minus threshold
        }
        if((sig2 < (threshold2 + error)) && (sig2 > (threshold2 - error)))
        {
            flag2 = 1; // set flag if ssid2 have strength within range of error of 5 dBm plus minus threshold
        }
        if((sig3 < (threshold3 + error)) && (sig3 > (threshold3 - error)))
        {
            flag3 = 1; // set flag if ssid3 have strength within range of error of 5 dBm plus minus threshold
        }
        if(flag1 ==1 && flag2==1 && flag3==1) // check trilateration condition
        {
            return true;  // location identified
        }
        return false;  // location not identified
    }
}