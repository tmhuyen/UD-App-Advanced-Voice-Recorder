package com.example.udapp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import java.io.File;
import java.util.concurrent.TimeUnit;

public class MP3Helper {

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;

    // Method to get last modified time of an MP3 file
    public static long getLastModifiedTime(Context context, File mp3File) {
        if (mp3File != null && mp3File.exists()) {
            return mp3File.lastModified();
        } else {
            return 0; // Return 0 if file does not exist
        }
    }

    // Method to get duration of an MP3 file
    public static String getDurationFormatted(Context context, File mp3File) {
        // Check if the app has the READ_EXTERNAL_STORAGE permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If the permission is not granted, request it
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
        }
        if (mp3File != null && mp3File.exists() && mp3File.canRead()) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(mp3File.getAbsolutePath());
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release();

                if (duration == null) {
                    Log.e("MP3Helper", "Duration is null for file: " + mp3File.getAbsolutePath());
                    return "00:00:00";
                }

                long durationMillis = Long.parseLong(duration);
                long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis - TimeUnit.HOURS.toMillis(hours));
                long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis - TimeUnit.HOURS.toMillis(hours) - TimeUnit.MINUTES.toMillis(minutes));
                //Toast.makeText(context, "Duration: " + hours + ":" + minutes + ":" + seconds, Toast.LENGTH_SHORT).show();
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            } catch (Exception e) {
                Log.e("MP3Helper", "Error retrieving duration for file: " + mp3File.getAbsolutePath(), e);
                return "00:00:00";
            }
        } else {
            Log.e("MP3Helper", "File does not exist or cannot be read: " + (mp3File != null ? mp3File.getAbsolutePath() : "null"));
            return "00:00:00";
        }
    }
}
