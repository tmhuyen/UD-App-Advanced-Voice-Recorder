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
import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends ArrayAdapter<Record> {

    public RecordAdapter(Context context, ArrayList<Record> records) {
        super(context, 0, records);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_records, parent, false);
        }

        Record file = getItem(position);

        TextView recordName = convertView.findViewById(R.id.recordName);
        TextView recordInfo = convertView.findViewById(R.id.recordInfo);
        ImageView playIcon = convertView.findViewById(R.id.playIcon);

        recordName.setText(file.getFileName());
        recordInfo.setText(file.getDuration() + "  |  " + file.getTime());
        playIcon.setImageResource(R.drawable.baseline_play_arrow_48);

        return convertView;
    }
}