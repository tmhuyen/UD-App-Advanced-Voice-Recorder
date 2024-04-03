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
import java.util.Locale;

public class RecordingFragment extends Fragment {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private String fileName = null;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int seconds = 0;
    private Visualizer visualizer = null;
    private String audioFormat;

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

        fileName = getActivity().getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        recordButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else {
                onRecord(recorder == null);
            }
        });

        playButton.setOnClickListener(v -> onPlay(player == null));

        return view;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
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
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        switch (audioFormat) {
            case ".mp3":
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                break;
            case ".aac":
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                break;
            case ".wav":
                recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                break;
            default:
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                break;
        }
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();

        ImageView recordButton = getActivity().findViewById(R.id.recordBtn);
        ImageView playButton = getActivity().findViewById(R.id.playBtn);
        recordButton.setImageResource(R.drawable.recording_in_active);
        playButton.setImageResource(R.drawable.recording_pause);

        seconds = 0;
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
        recorder.stop();
        recorder.release();
        recorder = null;

        ImageView recordButton = getActivity().findViewById(R.id.recordBtn);
        ImageView playButton = getActivity().findViewById(R.id.playBtn);
        recordButton.setImageResource(R.drawable.recording_active);
        playButton.setImageResource(R.drawable.recording_play);
        TextView timeCounter = getActivity().findViewById(R.id.timeCounter);
        timeCounter.setText("00:00:00");
        // Show a Toast message with the file path
        TextView textPath = getActivity().findViewById(R.id.recordPath);
        textPath.setText(String.format("Recording saved to: %s", fileName));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecord(recorder == null);
            } else {
                Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}