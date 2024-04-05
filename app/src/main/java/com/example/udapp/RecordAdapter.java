package com.example.udapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class RecordAdapter extends ArrayAdapter<File> {

    public RecordAdapter(@NonNull Context context, File[] files) {
        super(context, 0, files);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_records, parent, false);
        }

        File file = getItem(position);

        TextView recordName = convertView.findViewById(R.id.recordName);
        TextView recordInfo = convertView.findViewById(R.id.recordInfo);
        ImageView playIcon = convertView.findViewById(R.id.playIcon);

        recordName.setText(file.getName());
        recordInfo.setText("Information of the record | Time of the record");
        playIcon.setImageResource(R.drawable.baseline_play_arrow_48);

        return convertView;
    }
}