package com.example.udapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.ClientStream;
import com.google.api.gax.rpc.ResponseObserver;
import com.google.api.gax.rpc.StreamController;

import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognizeResponse;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.RecognizeRequest;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionResult;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionConfig;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.speech.v1p1beta1.SpeechSettings;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1p1beta1.StreamingRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.StreamingRecognitionResult;
import com.google.cloud.speech.v1p1beta1.WordInfo;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.protobuf.ByteString;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1p1beta1.SpeechSettings;


import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RecordingFragment extends Fragment {

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1001;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1002;
    private MyDB myDB;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;
    private String fileName = null;
    private Handler handler = new Handler();
    private Runnable runnable;
    private int seconds = 0;
    private Visualizer visualizer = null;
    private boolean isRecording = false;
    private StorageReference storageReference;
    private DatabaseReference dbRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;
    ImageView playButton;

    private int duration;

    private String selectedAudioSource;
    private String selectedOutputFormat;
    private int audioSource;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the arguments from the Bundle
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Retrieve the selected audio source and output format
            selectedAudioSource = bundle.getString("selectedAudioSource");
            selectedOutputFormat = bundle.getString("selectedOutputFormat");
        }
        // Map the selected audio source to the corresponding constant

        if ("MIC".equals(selectedAudioSource)) {
            audioSource = MediaRecorder.AudioSource.MIC;
        } else if ("VOICE_COMMUNICATION".equals(selectedAudioSource)) {
            audioSource = MediaRecorder.AudioSource.VOICE_COMMUNICATION;
        } else if ("VOICE_RECOGNITION".equals(selectedAudioSource)) {
            audioSource = MediaRecorder.AudioSource.VOICE_RECOGNITION;
        } else {
            audioSource = MediaRecorder.AudioSource.DEFAULT;
        }
        //Update format text
        TextView formatText = getActivity().findViewById(R.id.recordFormat);
        formatText.setText("Format: " + selectedOutputFormat);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recording, container, false);
        
        ImageView recordButton = view.findViewById(R.id.recordBtn);
        playButton = view.findViewById(R.id.playBtn);

        // Initialize Firebase Storage
        storageReference = FirebaseStorage.getInstance().getReference();


        recordButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            } else if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
            } else if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
            }
            else {
                seconds = 0;
                onRecord();
            }
        });

        playButton.setOnClickListener(v -> onPause(isRecording));

        myDB = new MyDB(getActivity());

        return view;
    }

    private void onPause(boolean start) {
        if (start) {
            pauseRecording();
        } else {
            resumeRecording();
        }
    }

    private void pauseRecording() {
        if (recorder != null) {
            recorder.pause();
            // Change the button image to a "resume" icon
            playButton.setImageResource(R.drawable.recording_play);
        }
    }

    private void resumeRecording() {
        if (recorder != null) {
            recorder.resume();
            // Change the button image to a "pause" icon
            playButton.setImageResource(R.drawable.recording_pause);
        }
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
            recorder.setAudioSource(audioSource);
            if (selectedOutputFormat == null) {
                selectedOutputFormat = "mp3";
            }
            else if (selectedOutputFormat.equals("mp3")||selectedOutputFormat.equals("MP3")||selectedOutputFormat.equals("Mp3")){
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
            else if (selectedOutputFormat.equals("aac")||selectedOutputFormat.equals("AAC")||selectedOutputFormat.equals("Aac")) {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
            else if (selectedOutputFormat.equals("wav")||selectedOutputFormat.equals("WAV")||selectedOutputFormat.equals("Wav")) {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            //fileName = getActivity().getExternalCacheDir().getAbsolutePath();
            File tempFile = File.createTempFile("audio", "." + selectedOutputFormat, getActivity().getCacheDir());
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int secs = seconds % 60;

            String time = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs);
            //fileName+= "/audio"+ dtf.format(now)+"." + selectedOutputFormat;
            fileName = tempFile.getAbsolutePath();
            recorder.setOutputFile(fileName);
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
            //Toast.makeText(getActivity(), "Failed to start recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                duration = seconds;
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
                //Toast.makeText(getActivity(), "Failed to stop recording: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            recorder.release();
            recorder = null;
            isRecording = false;
        }
        handler.removeCallbacks(runnable);

        // Upload the audio file to Firebase Storage
        uploadAudioToFirebase(fileName);

        // Save the recording to the database
        myDB.insertRecording(fileName);
        // Show a Toast message with the file path
        TextView textPath = getActivity().findViewById(R.id.recordPath);
        textPath.setText(String.format("Recording saved to: %s", fileName));
        System.out.println("Recording saved to: " + fileName);

        String transcribedText = "";
        try {
            transcribedText = transcribeAudio(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            // Handle transcription error
        }
        TextView textTranscription = getActivity().findViewById(R.id.textTranscription);
        textTranscription.setText("Transcription: " + transcribedText);

        // Delete the temporary file
        new File(fileName).delete();
    }

    private void uploadAudioToFirebase(String filePath) {
        Toast.makeText(getActivity(), "Uploading audio to Firebase Storage " + filePath, Toast.LENGTH_SHORT).show();
        Log.d("RecordingFragment", "Uploading audio to Firebase Storage " + filePath);
        if (filePath != null) {
            // Create a reference to the audio file in Firebase Storage
            String fileName = "audio_" + System.currentTimeMillis() + ".mp3"; // Adjust file name as needed
            StorageReference audioRef = storageReference.child("audio").child(fileName);

            // Upload the file to Firebase Storage
            UploadTask uploadTask = audioRef.putFile(Uri.fromFile(new File(filePath)));

            // Register observers to listen for upload success or failure
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Handle successful upload
                //Toast.makeText(getActivity(), "Upload successful", Toast.LENGTH_SHORT).show();
                // Get the download URL
                audioRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();

                    // Save the download URL to Firebase Realtime Database or Firestore if needed
                    mAuth = FirebaseAuth.getInstance();
                    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build();
                    mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                    FirebaseUser user = mAuth.getCurrentUser();

                    SharedPreferences settings = getActivity().getSharedPreferences("LoginPrefs", 0);
                    String name = settings.getString("username", "");

                    dbRef = FirebaseDatabase.getInstance().getReference("records");
                    String recordId = dbRef.push().getKey();
                    MediaPlayer mediaPlayer = new MediaPlayer();

                    LocalDateTime now = LocalDateTime.now();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
                    String uploadDate = dtf.format(now);

                    RecordFirebase recordFB;
                    if (user != null) {
                        recordFB = new RecordFirebase(recordId, user.getDisplayName(), downloadUrl, fileName, duration, uploadDate);
                    } else {
                        recordFB = new RecordFirebase(recordId, name, downloadUrl, fileName, duration, uploadDate);
                    }

                    dbRef.child(recordId).setValue(recordFB)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Record saved to Firebase", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Failed to save record to Firebase", Toast.LENGTH_SHORT).show();
                                }
                            });


                });
            }).addOnFailureListener(exception -> {
                // Handle failed upload
                Toast.makeText(getActivity(), "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onRecord();
            } else {
                //Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String transcribeAudio(String filePath) throws IOException {
        // Replace with your Google Cloud project ID
        String projectId = "record-voice-f7bc5";
        Log.d("RecordingFragment", "Transcribing audio file: " + filePath);

        // Create a SpeechClient instance
        try (SpeechClient speechClient = SpeechClient.create()) {

            // Set audio content
            ByteString audioContent = ByteString.readFrom(new FileInputStream(filePath));

            // Configure request with recognition settings
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.MP3) // Set encoding based on your format
                    .setSampleRateHertz(16000) // Set sample rate (adjust if needed)
                    .setLanguageCode("en-US") // Set language code (change for other languages)
                    .build();

            // Create recognition audio
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setContent(audioContent)
                    .build();

            // Create recognize request
            RecognizeRequest request = RecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(audio)
                    .build();

//            // Perform speech recognition
//            OperationFuture<RecognizeResponse> responseFuture = speechClient.recognizeAsync(request);
//
//            while (!responseFuture.isDone()) {
//                // Do something while waiting for response
//            }
//
//            // Get transcribed text
//            RecognizeResponse response = responseFuture.get();
//            for (SpeechRecognitionAlternative alternative : response.getAlternativesList()) {
//                return alternative.getTranscript();
//            }

            // Perform speech recognition
            RecognizeResponse response = speechClient.recognize(request);
            Log.d("RecordingFragment", "Number of results: " + response.getResultsList().size());

            // Get transcribed text
            for (SpeechRecognitionResult result : response.getResultsList()) {
                for (SpeechRecognitionAlternative alternative : result.getAlternativesList()) {
                    Log.d("RecordingFragment", "Transcription: " + alternative.getTranscript());
                    return alternative.getTranscript();
                }
            }
        } catch (IOException e) {
            Log.e("RecordingFragment", "Error transcribing audio: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e("RecordingFragment", "Unexpected error: " + e.getMessage(), e);
        }

        return "";
    }

}