package com.example.udapp;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;

public class PlayBackground extends Service {
    private static final String TAG = "PlayBackground";
    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("fileName")) {
            String fileName = intent.getStringExtra("fileName");
            Log.d(TAG, "onStartCommand: Playing recording: " + fileName);
            playRecording(fileName);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        Log.d(TAG, "onDestroy: Service destroyed");
    }

    private void playRecording(String fileName) {
        player = new MediaPlayer();
        Log.e(TAG, "playRecording: Playing recording");
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "playRecording: Failed to play recording", e);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
