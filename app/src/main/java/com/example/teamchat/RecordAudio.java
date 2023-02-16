package com.example.teamchat;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

public class RecordAudio {
    // Creating a variable for media recorder object class
    private MediaRecorder mRecorder;

    // String variable is created for storing a file name
    private MediaPlayer mPLayer;

    // String variable is created for storing a file name
    private static String mFileName = null;
    public static void getRecording(){
    }

    private void startRecording(){
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/AudioRecording.3gp";

        // Below method is used to initialize the media recorder class
        mRecorder = new MediaRecorder();

        // Below method is used to set the audio source which we are using a mic
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        // Below Method is used to set the audio encoder for our recorded auido.
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Below method is used to set the output file location for our recorded audio
        mRecorder.setOutputFile(mFileName);
        try{
            // Below method will prepare our audio recorder class
            mRecorder.prepare();
        }catch(IOException e){
            Log.e("TAG", "Prepare() failed");
        }
        // Start method will start the audio recording
        mRecorder.start();
    }

    public void pauseRecording(){
        // Below method will stop the audio recording
        mRecorder.stop();

        // Below method will release the media recorder class
        mRecorder.release();
        mRecorder = null;
    }
}
