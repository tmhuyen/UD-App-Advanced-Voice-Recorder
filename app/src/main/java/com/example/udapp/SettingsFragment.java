package com.example.udapp;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

        // Get the audio manager
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        // Get the list of audio devices
        AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);

        // Create a list of strings to hold the names of the audio devices
        List<String> audioDeviceNames = new ArrayList<>();

        // Populate the list with the names of the audio devices
        for (AudioDeviceInfo deviceInfo : audioDevices) {
            if (deviceInfo.getType() == AudioDeviceInfo.TYPE_BUILTIN_MIC) {
                audioDeviceNames.add(deviceInfo.getProductName().toString());
            }
        }

        // Get the spinner from the layout
        Spinner spinner = view.findViewById(R.id.audio_source_spinner);

        // Create an ArrayAdapter using the list of audio device names and set it to the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, audioDeviceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        List<String> outputFormats = new ArrayList<>();
        outputFormats.add("MP3");
        outputFormats.add("WAV");
        outputFormats.add("AAC");

        Spinner outputFormatSpinner = view.findViewById(R.id.output_format_spinner);
        ArrayAdapter<String> outputFormatAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, outputFormats);
        outputFormatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        outputFormatSpinner.setAdapter(outputFormatAdapter);

        Button applyButton = view.findViewById(R.id.apply_btn);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the selected items from the spinners
                String selectedAudioSource = spinner.getSelectedItem().toString();
                String selectedOutputFormat = outputFormatSpinner.getSelectedItem().toString();

                // Save the selections using SharedPreferences
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("selectedAudioSource", selectedAudioSource);
                editor.putString("selectedOutputFormat", selectedOutputFormat);
                editor.apply();

                // Create a Bundle and put the selected items into it
                Bundle bundle = new Bundle();
                bundle.putString("selectedAudioSource", selectedAudioSource);
                bundle.putString("selectedOutputFormat", selectedOutputFormat);


                // Create an instance of RecordingFragment and set the Bundle as its arguments
                RecordingFragment recordingFragment = RecordingFragment.getInstance();
                recordingFragment.setArguments(bundle);

                // Replace the current fragment with the RecordingFragment instance
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, recordingFragment).commit();
                //Change the navigation bar to the recording fragment
                getActivity().findViewById(R.id.recording).performClick();
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onPause() {
        super.onPause();
        // When the fragment is paused, send the selected audio source, output format, and path to the RecordingFragment

    }
}