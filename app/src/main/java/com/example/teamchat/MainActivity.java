package com.example.teamchat;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Drive mDriveService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v("test1:", "onCreate");

        startService(new Intent(this, ChatService.class));

        TeammateInfoActivity.bindButton(this, R.id.member1,
                getResources().getString(R.string.member1), getResources().getString(R.string.member1_age), getResources().getString(R.string.member1_hobby));
        TeammateInfoActivity.bindButton(this, R.id.member2,
                getResources().getString(R.string.member2), getResources().getString(R.string.member2_age), getResources().getString(R.string.member2_hobby));
        TeammateInfoActivity.bindButton(this, R.id.member3,
                getResources().getString(R.string.member3), getResources().getString(R.string.member3_age), getResources().getString(R.string.member3_hobby));
        TeammateInfoActivity.bindButton(this, R.id.member4,
                getResources().getString(R.string.member4), getResources().getString(R.string.member4_age), getResources().getString(R.string.member4_hobby));
        TeammateInfoActivity.bindButton(this, R.id.member5,
                getResources().getString(R.string.member5), getResources().getString(R.string.member5_age), getResources().getString(R.string.member5_hobby));
        TeammateInfoActivity.bindButton(this, R.id.member6,
                getResources().getString(R.string.member6), getResources().getString(R.string.member6_age), getResources().getString(R.string.member6_hobby));
    }

    public class ReadSMSAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SmsReader.readSMS(MainActivity.this, "test");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}







