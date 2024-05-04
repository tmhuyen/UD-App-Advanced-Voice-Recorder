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
        RecordFirebase record = records.get(position);
        StorageReference oldRef = FirebaseStorage.getInstance().getReferenceFromUrl(record.getDownloadUrl());
        StorageReference newRef = oldRef.getParent().child(newName);
        File localTempFile = null;
        try {
            localTempFile = File.createTempFile("tempFile", "tmp");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File finalLocalTempFile = localTempFile;
        oldRef.getFile(localTempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                newRef.putFile(Uri.fromFile(finalLocalTempFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // File copied successfully
                        oldRef.delete(); // Delete the old file
                        record.setFileName(newName); // Update the record object
                        //Update the new name in firebase
                        FirebaseStorage.getInstance().getReference().child("records").child(record.getFileName()).delete();
                        FirebaseStorage.getInstance().getReference().child("records").child(newName).putFile(Uri.fromFile(finalLocalTempFile));
                        notifyDataSetChanged(); // Refresh the list
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        Toast.makeText(context, "Failed to rename file", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle any errors
            }
        });
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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // User is signed in, you can try to get a token
            auth.getCurrentUser().getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                // Proceed with deletion
                                if (position < 0 || position >= records.size()) {
                                    // Position is not valid, do not proceed
                                    return;
                                }

                                RecordFirebase record = records.get(position);
                                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(record.getDownloadUrl());

                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // File exists, proceed with deletion
                                        storageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // File deleted successfully
                                                // Delete the record from the list
                                                records.remove(position); // Remove the record from the list
                                                // Delete the record from the database
                                                FirebaseStorage.getInstance().getReference().child("records").child(record.getFileName()).delete();
                                                notifyDataSetChanged(); // Refresh the list

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle any errors
                                                Toast.makeText(context, "Failed to delete file", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // File does not exist, handle the error
                                        if (e instanceof IOException && e.getMessage().contains("\"code\": 404")) {
                                            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Other errors
                                            Toast.makeText(context, "An error occurred", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                // Handle error -> task.getException();
                            }
                        }
                    });
        } else {
            // No user is signed in
            Toast.makeText(context, "Please sign in before trying to get a token.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = new ArrayList<>(records); // 'records' is your original list.
                    results.count = records.size();
                } else {
                    List<RecordFirebase> filteredList = new ArrayList<>();
                    for (RecordFirebase record : records) {
                        if (record.getFileName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filteredList.add(record);
                        }
                    }
                    results.values = filteredList;
                    results.count = filteredList.size();
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