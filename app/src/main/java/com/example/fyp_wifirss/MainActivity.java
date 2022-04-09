package com.example.fyp_wifirss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.fyp_wifirss.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity{
    private static final String TAG = "sensor";
    private WifiManager wm;
    private List<ScanResult> results;   //temp storage of WiFi records created by wm
    private int count = 5;
    StringBuilder sb = new StringBuilder(); //save data for CSV saving
    StringBuilder setText = new StringBuilder(); //save data for textView display
    String filecode; //filecode is the file name of the file, will be generated when the saveTextAsFile was executed

    //initialize the sensors and text fields, buttons
    TextView string;
    Button b_save;
    EditText x_cor, y_cor, z_cor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        Log.d(TAG, "App started");
        setContentView(R.layout.activity_main);
        b_save = (Button) findViewById(R.id.b_save);
        x_cor = (EditText) findViewById(R.id.x_cor);
        y_cor = (EditText) findViewById(R.id.y_cor);
        z_cor = (EditText) findViewById(R.id.z_cor);

        //setting of wifi manager to scan wifi
        wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wm.isWifiEnabled()) {
            wm.setWifiEnabled(true);
        }
        string = (TextView) findViewById(R.id.string); // textview for showing data when saving CSV file
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }


    private void saveTextAsFile(){
        //Create filename using current timestamp
        filecode = System.nanoTime()+"";
        String fileName = filecode+".csv";
        File file = new File(Environment.getExternalStorageDirectory().toString()+"/download",fileName); //save to download directory of the phone in CSV format
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();
            Log.d(TAG, "File Saved");
            setText.setLength(0);
            sb.setLength(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
            Toast.makeText(this,"Cannot Save",Toast.LENGTH_SHORT).show();
        }

    }


    private void scanWifi(){
        registerReceiver(wifiReceiver, new IntentFilter(wm.SCAN_RESULTS_AVAILABLE_ACTION));
        //start scanning wifi
        wm.startScan();
    }
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wm.getScanResults();
            //add coordinates in first three row of the CSV
            sb.append(x_cor.getText().toString());
            sb.append('\n');
            sb.append(y_cor.getText().toString());
            sb.append('\n');
            sb.append(z_cor.getText().toString());
            sb.append('\n');
            //show instructive information
            setText.append("This is the  "+(6-count)+" scan");
            setText.append(System.getProperty("line.separator"));
            setText.append("This and the above line will NOT be included in csv");
            setText.append(System.getProperty("line.separator"));
            //add coordinates in the textView
            setText.append(x_cor.getText().toString());
            setText.append(System.getProperty("line.separator"));
            setText.append(y_cor.getText().toString());
            setText.append(System.getProperty("line.separator"));
            setText.append(z_cor.getText().toString());
            setText.append(System.getProperty("line.separator"));
            unregisterReceiver(this);
            for(ScanResult scanResult : results){
                //append text for CSV saving
                sb.append(scanResult.BSSID + "," + scanResult.level);
                sb.append('\n');
                //append text for textView display
                setText.append(scanResult.BSSID + "," + scanResult.level);
                setText.append(System.getProperty("line.separator"));
            }
            string.setText(setText);
            Log.d(TAG, "Wifi Scanned");
            saveTextAsFile();
            if(--count>0){
                //recursive for next radio measurement if not finished
                scanWifi();
            }
            else{
                //show alert message if the scan completed
                end();
            }

        };
    };


    public void end(){
        Toast.makeText(this,"Scan completed!",Toast.LENGTH_SHORT).show();
    }
    public void b_onClick(View view) {
                count=5;
                //hide keyboard after the button is pressed
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                scanWifi();
                filecode = System.nanoTime()+"";



    }
}