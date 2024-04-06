package com.example.udapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.AdapterView;
import android.media.MediaPlayer;
import java.io.File;
import java.io.IOException;
import android.widget.ArrayAdapter;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class RecordsFragment extends Fragment {

    MyDB myDB;
    private ListView recordsListView;
    private List<Record> records; // Define records as a class member variable // Define records as a class member variable
    private File[] audioFiles;
    private MediaPlayer player = null;

    public RecordsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        myDB = new MyDB(getActivity()); // Initialize myDB here

        recordsListView = view.findViewById(R.id.recordsList);
        recordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playRecording(records.get(position));
            }
        });

        loadRecordings();

        return view;
    }

    private void loadRecordings() {
        // Load recordings from the external cache directory
        records = new ArrayList<>();
        audioFiles = getActivity().getExternalCacheDir().listFiles();
        for (File file : audioFiles) {
            Record record = new Record(file.getName(), file.length(), file.lastModified());
            records.add(record);
        }
        // Create a RecordAdapter object
        RecordAdapter adapter = new RecordAdapter(getActivity(), records);
        // Set the adapter for the recordsListView
        recordsListView.setAdapter(adapter);
    }



    private void playRecording(Record record) {
        player = new MediaPlayer();
        try {
            File audioFile = new File(record.getFileName());
            player.setDataSource(audioFile.getAbsolutePath());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}

//Intent intentCallService4 = new Intent(this, PlayBackground.class);
//startService(intentCallService4);