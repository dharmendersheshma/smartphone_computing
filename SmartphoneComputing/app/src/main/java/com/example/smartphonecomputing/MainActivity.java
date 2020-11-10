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
import android.content.SharedPreferences;
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

    public static final String MyPREFERENCES = "WifiInfo" ;
    static SharedPreferences pref;

    static String ssidI1;
    static String ssidI2;
    static String ssidI3;

    static String ssidII1;
    static String ssidII2;
    static String ssidII3;

    static int thresholdI1; //Set signal threshold for ssid 1
    static int thresholdI2;  //Set signal threshold for ssid 2
    static int thresholdI3;  //Set signal threshold for ssid 3

    static int thresholdII1; //Set signal threshold for ssid 1
    static int thresholdII2;  //Set signal threshold for ssid 2
    static int thresholdII3;  //Set signal threshold for ssid 3

    //Current signal strength for above three ssids
    int sigI1;
    int sigI2;
    int sigI3;

    int sigII1;
    int sigII2;
    int sigII3;

    static int ErrorI;
    static int ErrorII;

    int fflag = -1;
    boolean outside1 = true;
    boolean outside2 = true;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Permission for location services
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, 1);

        pref = getApplicationContext().getSharedPreferences(MyPREFERENCES, 0); // 0 :- for private mode



        buttonScan = findViewById(R.id.scanBtn);
        buttonSelectWifi = findViewById(R.id.user_selection);
        buttonInfo = findViewById(R.id.saved_info);

        buttonInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "SSIDI1: " + ssidI1 + " strength: " + thresholdI1 + "dBm" + "\n" +
                        "SSIDI2: " + ssidI2 + " strength: " + thresholdI2 + "dBm" + "\n" +
                        "SSIDI3: " + ssidI3 + " strength: " + thresholdI3 + "dBm" + "\n" +
                        "ErrorI: " + ErrorI + "\n" +
                        "SSIDII1: " + ssidII1 + " strength: " + thresholdII1 + "dBm" + "\n" +
                        "SSIDII2: " + ssidII2 + " strength: " + thresholdII2 + "dBm" + "\n" +
                        "SSIDII3: " + ssidII3 + " strength: " + thresholdII3 + "dBm" + "\n" +
                        "ErrorII: " + ErrorII, Toast.LENGTH_LONG).show();
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
    protected void onResume() {
        super.onResume();

        ssidI1 = pref.getString("idI1", null); // getting String; //Set ssid 1
        ssidI2 = pref.getString("idI2", null);//Set ssid 2
        ssidI3 = pref.getString("idI3", null); //Set ssid 3

        ssidII1 = pref.getString("idII1", null); //Set ssid 1
        ssidII2 = pref.getString("idII2", null); //Set ssid 2
        ssidII3 = pref.getString("idII3", null); //Set ssid 3

        thresholdI1 = pref.getInt("tdI1", 0); // getting Integer
        thresholdI2 = pref.getInt("tdI2", 0); // getting Integer
        thresholdI3 = pref.getInt("tdI3", 0); // getting Integer

        thresholdII1 = pref.getInt("tdII1", 0); // getting Integer
        thresholdII2 = pref.getInt("tdII2", 0); // getting Integer
        thresholdII3 = pref.getInt("tdII3", 0); // getting Integer

        ErrorI = pref.getInt("errorI", 5); // getting Integer
        ErrorII = pref.getInt("errorII", 5); // getting Integer
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
        Toast.makeText(MainActivity.this, "Scanning stopped", Toast.LENGTH_SHORT).show();
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
                int count1 = 0; // to check all three ssids which we require are available
                int count2 = 0;
                for (ScanResult scanResult : results) {

                    if(ssidI1.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid1"
                    {
                        sigI1 = scanResult.level;   // set current signal strength of ssid1
                        count1++;
                    }
                    else if(ssidI2.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid2"
                    {
                        sigI2 = scanResult.level;  // set current signal strength of ssid2
                        count1++;
                    }
                    else if(ssidI3.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid3"
                    {
                        sigI3 = scanResult.level;  // set current signal strength of ssid3
                        count1++;
                    }
                    else if(ssidII1.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid1"
                    {
                        sigII1 = scanResult.level;   // set current signal strength of ssid1
                        count2++;
                    }
                    else if(ssidII2.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid2"
                    {
                        sigII2 = scanResult.level;  // set current signal strength of ssid2
                        count2++;
                    }
                    else if(ssidII3.equals(scanResult.SSID))   //Checking our known ssid defined above as "ssid3"
                    {
                        sigII3 = scanResult.level;  // set current signal strength of ssid3
                        count2++;
                    }
                    int f1 = 0;
                    if(count1==3)  // if all three required wifi access points are available
                    {
                        fflag = 1;
                        f1 = 1;
                        boolean check = check_location(thresholdI1, thresholdI2, thresholdI3, sigI1, sigI2, sigI3, ErrorI); // check if we are at required location
                        if(check && outside1)
                        {
                            sendNotification(this); //Give notification to user
                            outside1 = false;
                            Toast.makeText(getApplicationContext(),"Location 1 idetified!",Toast.LENGTH_SHORT).show();  //Toast
                        }
                    }
                    if(count2==3)  // if all three required wifi access points are available
                    {
                        fflag = 2;
                        f1 = 1;
                        boolean check = check_location(thresholdII1, thresholdII2, thresholdII3, sigII1, sigII2, sigII3, ErrorII); // check if we are at required location
                        if(check && outside2)
                        {
                            sendNotification(this); //Give notification to user
                            outside2 = false;
                            Toast.makeText(getApplicationContext(),"Location 2 idetified!",Toast.LENGTH_SHORT).show();  //Toast
                        }
                    }
                    if(f1==0)
                    {
                        outside1 = true;
                        outside2 = true;
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
        if(fflag == 1) {
            builder = builder
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentText("Trilateration notifiction....You are in Balcony! Do you want to turn on Music?")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true);
        }
        else
        {
            builder = builder
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentText("Trilateration notifiction....Hey you are in living room! Do you want to turn TV on?")
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true);
        }
        fflag = 0;
        notificationManager.notify(01, builder.build());
    }

    //Trilateration function
    public boolean check_location(int threshold1, int threshold2, int threshold3, int sig1, int sig2, int sig3, int error){
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