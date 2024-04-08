package com.example.udapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.media.MediaPlayer;
import android.widget.Toast;
import android.content.Context;

import java.io.File;
import java.io.IOException;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class RecordsFragment extends Fragment {

    private ListView recordsListView;
    private List<Record> records;
    private File[] audioFiles;
    private MediaPlayer player = null;

    public RecordsFragment() {
    }

    public File[] getAllFilesInExternalCache(Context context) {
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            return externalCacheDir.listFiles();
        }
        return new File[0];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        recordsListView = view.findViewById(R.id.recordsList);
        records = new ArrayList<>();

        loadRecordings();

        RecordAdapter adapter = new RecordAdapter(getActivity(), (ArrayList<Record>) records);
        recordsListView.setAdapter(adapter);

//        recordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                playRecording(records.get(position));
//            }
//        });

        return view;
    }

    private void loadRecordings() {
        audioFiles = getAllFilesInExternalCache(getActivity());
        for (File file : audioFiles) {
            records.add(new Record(file.getName(), MP3Helper.getDuration(getActivity(), file), MP3Helper.getLastModifiedTime(getActivity(), file)));
        }
    }

    private void playRecording(String record) {
        player = new MediaPlayer();
        try {
            File audioFile = new File(getActivity().getExternalCacheDir(), record);
            player.setDataSource(audioFile.getAbsolutePath());
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}