package com.example.udapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecordAdapter extends ArrayAdapter<RecordFirebase> {
    private List<RecordFirebase> filteredRecords;
    private List<RecordFirebase> records;
    private Context context;
    public RecordAdapter(Context context, ArrayList<RecordFirebase> records) {

        super(context, 0, records);
        this.records = new ArrayList<>(records);
        this.filteredRecords = new ArrayList<>(records);
        this.context = context;
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
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("RecordAdapter", "getView called for position " + position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_records, parent, false);
        }

        RecordFirebase file = getItem(position);

        TextView recordName = convertView.findViewById(R.id.recordName);
        TextView recordInfo = convertView.findViewById(R.id.recordInfo);
        ImageView note = convertView.findViewById(R.id.editNoteIcon);

        recordName.setText(file.getFileName());
        recordInfo.setText(String.valueOf(file.getDuration()) + " seconds | " + file.getUploadDate());
        note.setImageResource(R.drawable.baseline_edit_note_24);

        convertView.setOnTouchListener(new View.OnTouchListener() {
            float downXValue;


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        downXValue = event.getX();
                        return true;

                    case MotionEvent.ACTION_UP:
                        float currentX = event.getX();
                        if (downXValue < currentX) {
                            // User swiped from left to right
                            showRenameDialog(position);
                        }
                        if (downXValue > currentX) {
                            // User swiped from right to left
                            showDeleteDialog(position);
                        }
                        return true;
                }
                return false;
            }
        });

        return convertView;
    }

    private void showRenameDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename File");

        final EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = input.getText().toString();
                //Check if the new name is empty
                if (newName.isEmpty()) {
                    Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Check if the new name already exists
                for (RecordFirebase record : records) {
                    if (record.getFileName().equals(newName)) {
                        Toast.makeText(context, "File with this name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                renameFileInFirebase(position, newName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void renameFileInFirebase(int position, String newName) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("records");
        RecordFirebase record = records.get(position);
        record.setFileName(newName);
        dbRef.child(record.getRecordId()).setValue(record);
        notifyDataSetChanged();
    }

    private void showDeleteDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete File");

        builder.setMessage("Are you sure you want to delete this file?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFileInFirebase(position);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteFileInFirebase(int position) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("records");
        RecordFirebase record = records.get(position);
        record.setUsername("");
        dbRef.child(record.getRecordId()).setValue(record);
        notifyDataSetChanged();
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