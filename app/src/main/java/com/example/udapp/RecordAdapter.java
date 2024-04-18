package com.example.udapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends ArrayAdapter<RecordFirebase> {
    private List<RecordFirebase> filteredRecords;
    private List<RecordFirebase> records;

    public RecordAdapter(Context context, ArrayList<RecordFirebase> records) {

        super(context, 0, records);
        this.records = new ArrayList<>(records);
        this.filteredRecords = new ArrayList<>(records);
    }
    @Override
    public int getCount() {
        Log.d("RecordAdapter", "getCount called, size: " + filteredRecords.size());
        return filteredRecords.size();
    }
    @Override
    public RecordFirebase getItem(int position) {
        return filteredRecords.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("RecordAdapter", "getView called for position " + position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_records, parent, false);
        }

        RecordFirebase file = getItem(position);

        TextView recordName = convertView.findViewById(R.id.recordName);
        TextView recordInfo = convertView.findViewById(R.id.recordInfo);
        ImageView note = convertView.findViewById(R.id.editNoteIcon);

        recordName.setText(file.getFileName());
        recordInfo.setText(String.valueOf(file.getDuration()));
        note.setImageResource(R.drawable.baseline_edit_note_24);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                Log.d("RecordAdapter", "performFiltering called with constraint: " + constraint);
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = records; // 'records' is your original list.
                    results.count = records.size();
                } else {
                    filteredRecords = new ArrayList<>();
                    for (RecordFirebase record : records) {
                        if (record.getFileName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            Log.d("RecordAdapter", "performFiltering: " + record.getFileName().toLowerCase());
                            Log.e("RecordAdapter", "performFiltering: " + constraint.toString().toLowerCase());
                            filteredRecords.add(record);
                        }
                    }
                    results.values = filteredRecords;
                    results.count = filteredRecords.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredRecords = (List<RecordFirebase>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}