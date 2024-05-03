package com.example.udapp;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> selectedAudioSource = new MutableLiveData<>();
    private final MutableLiveData<String> selectedOutputFormat = new MutableLiveData<>();

    public void selectAudioSource(String audioSource) {
        selectedAudioSource.setValue(audioSource);
    }

    public LiveData<String> getSelectedAudioSource() {
        return selectedAudioSource;
    }

    public void selectOutputFormat(String outputFormat) {
        selectedOutputFormat.setValue(outputFormat);
    }

    public LiveData<String> getSelectedOutputFormat() {
        return selectedOutputFormat;
    }
}