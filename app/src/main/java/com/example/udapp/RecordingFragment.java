package com.example.udapp;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.concurrent.HandlerExecutor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecordingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecordingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    ImageView recordButton;
    ImageView playButton;
    TextView timeCounter;
    TextView recordFormat;
    View visualizerLineBar;
    boolean isRecording = false;
    boolean isPlaying = false;

    int seconds = 0, minutes = 0, hours =0;
    String format = null;
    String path = null;
    int dummySecond = 0;
    int playableSecond = 0;
    Handler handler;

    ExecutorService executorService = Executors.newSingleThreadExecutor();


    public RecordingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecordingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecordingFragment newInstance(String param1, String param2) {
        RecordingFragment fragment = new RecordingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_recording);
        getSupportActionBar().hide();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        recordButton = getView().findViewById(R.id.recordBtn);
        playButton = getView().findViewById(R.id.playBtn);
        timeCounter = getView().findViewById(R.id.timeCounter);
        recordFormat = getView().findViewById(R.id.recordFormat);
        visualizerLineBar = getView().findViewById(R.id.visualizerLineBar);
        mediaPlayer = new MediaPlayer();


        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkRecordingPermission()){
                    if (!isRecording){
                        isRecording = true;
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder = new MediaRecorder();
                                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                mediaRecorder.setOutputFile(getRecordingFilePath());
                                path = getRecordingFilePath();
                                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                                try {
                                    mediaRecorder.prepare();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                mediaRecorder.start();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playableSecond = 0;
                                        dummySecond = 0;
                                        seconds = 0;
                                        timeCounter.setText("00:00:00");
                                        recordButton.setImageResource(R.drawable.recording_active);
                                        visualizerLineBar.setVisibility(View.INVISIBLE);
                                        recordFormat.setText(".Mp3");
                                        runTimer();

                                    }
                                });
                            }
                        });
                    }
                    else {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                                mediaRecorder = null;
                                playableSecond = seconds;
                                dummySecond = seconds;
                                seconds = 0;
                                isRecording = false;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playableSecond = 0;
                                        dummySecond = 0;
                                        seconds = 0;
                                        recordButton.setImageResource(R.drawable.recording_in_active);
                                        visualizerLineBar.setVisibility(View.VISIBLE);
                                        recordFormat.setText(".Mp3");
                                        handler.removeCallbacksAndMessages(null);

                                    }
                                });
                            }
                        });
                    }
                }
                else {
                    requestPermission();
                }
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (path != null){
                    if (!isPlaying){
                        isPlaying = true;
                        try {
                            mediaPlayer.setDataSource(path);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            playButton.setImageResource(R.drawable.recording_pause);
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    playButton.setImageResource(R.drawable.recording_play);
                                    isPlaying = false;
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        playButton.setImageResource(R.drawable.recording_pause);
                        isPlaying = false;
                    }
                }
                else {
                    Toast.makeText(getActivity(), "No Recording Found", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void runOnUiThread(Runnable tapToRecord) {
    }

    public boolean checkRecordingPermission(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
            requestPermission();
            return false;
        }
        return true;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recording, container, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (permissionToRecord) {
                    Toast.makeText(getActivity(), "Permission Given", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getRecordingFilePath() {
        ContextWrapper contextWrapper = new ContextWrapper(getContext());
        File music = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(music, "testsfile" + "mp3");
        return file.getPath();
    }
}