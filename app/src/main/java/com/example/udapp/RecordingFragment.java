package com.example.udapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.chibde.visualizer.LineBarVisualizer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class RecordingFragment extends Fragment {

    private MyDB myDB;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private String fileName = null;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int seconds = 0;
    private Visualizer visualizer = null;
    private String audioFormat = "mp3";
    private boolean isRecording = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recording, container, false);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            fileName = bundle.getString("path");
            audioFormat = bundle.getString("format");
        }
        ImageView recordButton = view.findViewById(R.id.recordBtn);
        ImageView playButton = view.findViewById(R.id.playBtn);


        recordButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                seconds = 0;
                onRecord();
            }
        });

        playButton.setOnClickListener(v -> onPlay(player == null));

        myDB = new MyDB(getActivity());

        return view;
    }
    private void onRecord() {
        if (!isRecording) {
            startRecording();
            ImageView recordButton = getActivity().findViewById(R.id.recordBtn);
            ImageView playButton = getActivity().findViewById(R.id.playBtn);
            recordButton.setImageResource(R.drawable.recording_in_active);
            playButton.setImageResource(R.drawable.recording_pause);
            isRecording = true;
        } else {
            stopRecording();
            ImageView recordButton = getActivity().findViewById(R.id.recordBtn);
            ImageView playButton = getActivity().findViewById(R.id.playBtn);
            recordButton.setImageResource(R.drawable.recording_active);
            playButton.setImageResource(R.drawable.recording_play);
            TextView timeCounter = getActivity().findViewById(R.id.timeCounter);
            timeCounter.setText("00:00:00");
            isRecording = false;
        }
    }


    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            if (audioFormat == null) {
                audioFormat = "mp3";
            }
            else if (audioFormat.equals("mp3")){
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
            else if (audioFormat.equals("aac")) {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
            else if (audioFormat.equals("wav")) {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            fileName = getActivity().getExternalCacheDir().getAbsolutePath();
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;

            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
            fileName+= "audio"+ time + dtf.format(now)+"." + audioFormat;

            recorder.setOutputFile(fileName);
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to start recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        TextView timeCounter = getActivity().findViewById(R.id.timeCounter);
        runnable = new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
                timeCounter.setText(time);

                if (recorder != null) {
                    seconds++;
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(runnable);
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                recorder.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "Failed to stop recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            recorder.release();
            recorder = null;
            isRecording = false;
        }
        handler.removeCallbacks(runnable);
        // Show a Toast message with the file path
        TextView textPath = getActivity().findViewById(R.id.recordPath);
        textPath.setText(String.format("Recording saved to: %s", fileName));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecord();
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}