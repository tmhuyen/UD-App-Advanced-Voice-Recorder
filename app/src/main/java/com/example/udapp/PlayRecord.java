package com.example.udapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.view.View;

import com.chibde.visualizer.LineBarVisualizer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;


public class PlayRecord extends AppCompatActivity {
    MediaPlayer player;
    ImageView playIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record);

        playIcon = findViewById(R.id.playIcon);
        playIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRecord(view);
            }
        });


        Intent intent = getIntent();
        String downloadUrl = intent.getStringExtra("downloadUrl");
        String fileName = intent.getStringExtra("fileName");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(downloadUrl);

        File localFile = new File(this.getExternalCacheDir(), fileName);

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local temp file has been created
                Log.d("Firebase", "Download Success");
                player = new MediaPlayer();
                try {
                    player.setDataSource(localFile.getAbsolutePath());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Log.e("Firebase", "Download Failed");
            }
        });
    }
    public void playRecord(View view) {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                playIcon.setImageResource(R.drawable.recording_play); // change to play icon
            } else {
                LineBarVisualizer lineBarVisualizer = findViewById(R.id.visualizer);
                player.start();
                lineBarVisualizer.setPlayer(player.getAudioSessionId());
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}