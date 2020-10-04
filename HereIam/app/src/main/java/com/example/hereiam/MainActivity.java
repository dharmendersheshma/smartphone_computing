package com.example.hereiam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private TextView tt;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    WifiReceiver wifiReceiver;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] PERMS_INITIAL={
                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        ActivityCompat.requestPermissions(this, PERMS_INITIAL, 127);

        buttonScan = findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });
        tt = findViewById(R.id.text);

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);

        scanWifi();

    }

    private void scanWifi() {

        //arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID + " - " + scanResult.capabilities);

                adapter.notifyDataSetChanged();
            }
        }
    };
}
//import java.util.List;
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.net.wifi.ScanResult;
//import android.net.wifi.WifiManager;
//import android.os.Bundle;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.widget.TextView;
//
//public class MainActivity extends Activity {
//    TextView mainText;
//    WifiManager mainWifi;
//    WifiReceiver receiverWifi;
//    List<ScanResult> wifiList;
//    StringBuilder sb = new StringBuilder();
//
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        mainText = (TextView) findViewById(R.id.text);
//        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        receiverWifi = new WifiReceiver();
//        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        mainWifi.startScan();
//        mainText.setText("\\nStarting Scan...\\n");
//    }
//
//    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(0, 0, 0, "Refresh");
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    public boolean onMenuItemSelected(int featureId, MenuItem item) {
//        mainWifi.startScan();
//        mainText.setText("Starting Scan");
//        return super.onMenuItemSelected(featureId, item);
//    }
//
//    protected void onPause() {
//        unregisterReceiver(receiverWifi);
//        super.onPause();
//    }
//
//    protected void onResume() {
//        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        super.onResume();
//    }
//
//    class WifiReceiver extends BroadcastReceiver {
//        public void onReceive(Context c, Intent intent) {
//            sb = new StringBuilder();
//            wifiList = mainWifi.getScanResults();
//            for(int i = 0; i < wifiList.size(); i++){
//                sb.append(new Integer(i+1).toString() + ".");
//                sb.append((wifiList.get(i)).toString());
//                sb.append("\\n");
//            }
//            mainText.setText(sb);
//        }
//    }
//}