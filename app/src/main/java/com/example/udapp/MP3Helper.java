package com.example.udapp;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import java.io.File;

public class MP3Helper {

    // Method to get last modified time of an MP3 file
    public static long getLastModifiedTime(Context context, File mp3File) {
        if (mp3File != null && mp3File.exists()) {
            return mp3File.lastModified();
        } else {
            return 0; // Return 0 if file does not exist
        }
    }

    // Method to get duration of an MP3 file
    public static long getDuration(Context context, File mp3File) {
        if (mp3File != null && mp3File.exists()) {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(mp3File.getAbsolutePath());
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                retriever.release();
                return Long.parseLong(duration);
            } catch (Exception e) {
                e.printStackTrace();
                return 0; // Return 0 if unable to get duration
            }
        } else {
            return 0; // Return 0 if file does not exist
        }
    }
}
