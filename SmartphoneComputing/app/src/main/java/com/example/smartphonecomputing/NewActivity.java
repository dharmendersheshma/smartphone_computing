package com.example.smartphonecomputing;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewActivity extends AppCompatActivity {

    ArrayAdapter<String> adapter;
    WifiManager wifiManager;
    ArrayList<String> wifiList = new ArrayList<>();
    ListView listView;
    List<ScanResult> results;
    WifiReceiver wifiReceiver;
    Button buttonOK;
    Button buttonExit;
    EditText ERROR;

    String err;
    int count = 0;

    String[] sd = new String[3];
    String[] td = new String[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        //Permission for location services
        ActivityCompat.requestPermissions(NewActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION }, 1);

        ERROR = findViewById(R.id.inputnumber);

        buttonOK = findViewById(R.id.OK_buutton);
        //Button click listner for scanning wifi
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(count==3) {
                    MainActivity.ssid1 = sd[0];
                    MainActivity.ssid2 = sd[1];
                    MainActivity.ssid3 = sd[2];

                    MainActivity.threshold1 = Integer.parseInt(td[0]);
                    MainActivity.threshold2 = Integer.parseInt(td[1]);
                    MainActivity.threshold3 = Integer.parseInt(td[2]);

                    err = ERROR.getText().toString();
                    if(TextUtils.isEmpty(err))
                    {
                        MainActivity.error = 5;
                    }
                    else {
                        MainActivity.error = Integer.parseInt(err);
                    }

                    finish();
                }
                else if(count==3)
                {
                    Toast.makeText(NewActivity.this, "Input error for signal strength", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(NewActivity.this, "Select 3 different access points or click EXIT to return to home screen", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonExit = findViewById(R.id.exit_buutton);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        listView = findViewById(R.id.myList);  // list which we will polpulate by wifi access points


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                parent.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.colorChangeGray));
                String selectedFromList = (String) parent.getItemAtPosition(position);

                Pattern p = Pattern.compile( "Wifi SSID: \\s*([^\\n\\r]*)" );
                Matcher m = p.matcher( selectedFromList );
                if ( m.find() ) {
                    String s = m.group(1);
                    sd[count] = s;
                }
                Pattern pp = Pattern.compile( "[\\n\\r].*Wifi signal strength:\\s*([^\\n\\r]*)" );
                Matcher mm = pp.matcher( selectedFromList );
                if ( mm.find() ) {
                    String s = mm.group(1);
                    td[count] = s;
                }

                count++;
                if (count == 3 && ERROR!=null) {
                    listView.setOnItemClickListener(null);
                }
            }
        });


        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);  // create wifi manager

        wifiReceiver = new WifiReceiver();  // create wifi receiver

        if (!wifiManager.isWifiEnabled()) {   // checking if wifi is enabled or not
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<String>(NewActivity.this, android.R.layout.simple_list_item_1, wifiList);  // create adapter for handling list we create

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
                    wifiList.add("Wifi SSID: " + scanResult.SSID + "\n" + "Wifi signal strength: " +  scanResult.level + " dBm");  //Add available access points
                    adapter.notifyDataSetChanged();   // Change the list on new scan
                }
            }
        }
    };


}