package com.example.udapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private static final int REQUEST_CODE_PATH = 1;

    private String selectedPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        Spinner audioSourceSpinner = view.findViewById(R.id.audio_source_spinner);
        Spinner outputFormatSpinner = view.findViewById(R.id.output_format_spinner);
        Button savePathBtn = view.findViewById(R.id.save_path_btn);
        TextView pathTextView = view.findViewById(R.id.path_text_view);

        // Populate the audio source spinner
        List<Integer> audioDevices = getAudioDevices();
        ArrayAdapter<Integer> audioSourceAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, audioDevices);
        audioSourceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        audioSourceSpinner.setAdapter(audioSourceAdapter);

        // Populate the output format spinner
        List<String> audioFormats = new ArrayList<>();
        audioFormats.add(".mp3");
        audioFormats.add(".aac");
        audioFormats.add(".wav");
        ArrayAdapter<String> outputFormatAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, audioFormats);
        outputFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputFormatSpinner.setAdapter(outputFormatAdapter);

        // Set the save path button click listener
        savePathBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, REQUEST_CODE_PATH);
            // Display the path in the text view
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PATH && resultCode == Activity.RESULT_OK && data != null) {
            selectedPath = data.getData().getPath();
            TextView pathTextView = getView().findViewById(R.id.path_text_view);
            pathTextView.setText(selectedPath);
        }
    }

    private List<Integer> getAudioDevices() {
        List<Integer> devices = new ArrayList<>();
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
        for (AudioDeviceInfo device : audioDevices) {
            if (device.isSource()) {
                devices.add(device.getId());
            }
        }
        return devices;
    }

    @Override
    public void onPause() {
        super.onPause();
        // When the fragment is paused, send the selected audio source, output format, and path to the RecordingFragment
        Bundle bundle = new Bundle();
        bundle.putInt("audioSource", ((Spinner) getView().findViewById(R.id.audio_source_spinner)).getSelectedItemPosition());
        bundle.putString("outputFormat", ((Spinner) getView().findViewById(R.id.output_format_spinner)).getSelectedItem().toString());
        bundle.putString("path", selectedPath);
        RecordingFragment recordingFragment = new RecordingFragment();
        recordingFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, recordingFragment).commit();
    }
}