package com.example.udapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.media.MediaPlayer;
import android.widget.SearchView;
import android.widget.Toast;
import android.content.Context;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class RecordsFragment extends Fragment {
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 1002;
    private SearchView searchView;
    private ListView recordsListView;
    private List<RecordFirebase> records;
    private File[] audioFiles;
    private MediaPlayer player = null;
    private RecordAdapter adapter;
    private StorageReference storageReference;
    private DatabaseReference dbRef;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    GoogleSignInClient mGoogleSignInClient;
    GoogleSignInOptions gso;

    public RecordsFragment() {
    }

    public File[] getAllFilesInExternalCache(Context context) {
        if (ContextCompat.checkSelfPermission(getActivity(), "android.permission.READ_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
        }
        File externalCacheDir = context.getExternalCacheDir();
        if (externalCacheDir != null) {
            return externalCacheDir.listFiles();
        }
        return new File[0];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_records, container, false);

        recordsListView = view.findViewById(R.id.recordsList);
        records = new ArrayList<>();

        loadRecordings();
        Log.d("RecordsFragment", "Records: " + records.size());


        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }


        });
        recordsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //playRecording(String.valueOf(records.get(position)));
//                String fileName = getActivity().getExternalCacheDir().getAbsolutePath();
//                String filePath = records.get(position).getDownloadUrl();
//                fileName += "/" + filePath;
//                //playRecording(filePath);
//                String selectedFileName = records.get(position).getFileName();
//                Intent intent = new Intent(getActivity(), PlayBackground.class);
//                intent.putExtra("fileName", fileName);
//                Log.e("RecordsFragment", "Playing " + fileName);
//                getActivity().startService(intent);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(records.get(position).getDownloadUrl());

                File localFile = new File(getActivity().getExternalCacheDir(), records.get(position).getFileName());

                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        Log.d("Firebase", "Download Success");
                        player = new MediaPlayer();
                        try {
                            player.setDataSource(localFile.getAbsolutePath());
                            player.prepare();
                            player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (player != null && player.isPlaying()) {
                            player.stop();
                            player.release();
                            player = null;
                            localFile.delete();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.e("Firebase", "Download Failed");
                    }
                });
            }
        });

//        recordsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                Record selectedRecord = records.get(position);
//                String oldFileName = selectedRecord.getFileName();
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("Rename File");
//
//                final EditText input = new EditText(getActivity());
//                input.setInputType(InputType.TYPE_CLASS_TEXT);
//                builder.setView(input);
//
////                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String newFileName = input.getText().toString();
//                        //keep the recording time and format
//                        newFileName = newFileName + oldFileName.substring(oldFileName.lastIndexOf('|'));
//                        //example for old and newfileName
//                        //oldFileName = "audio|2021.09.29 14:00:00.mp3"
//                        //newFileName = "newName|2021.09.29 14:00:00.mp3"
//                        File oldFile = new File(getActivity().getExternalCacheDir(), oldFileName);
//                        File newFile = new File(getActivity().getExternalCacheDir(), newFileName);
//                        boolean renamed = oldFile.renameTo(newFile);
//                        if (renamed) {
//                            selectedRecord.setFileName(newFileName);
//                            ((ArrayAdapter) parent.getAdapter()).notifyDataSetChanged();
//                            Toast.makeText(getActivity(), "File renamed to: " + newFileName, Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(getActivity(), "Failed to rename file", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//                builder.show();
//                return true;
//            }
//        });
//
        recordsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                RecordFirebase record = (RecordFirebase) parent.getItemAtPosition(position);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(record.getDownloadUrl());

                File localFile = new File(getActivity().getExternalCacheDir(), record.getFileName());

                storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        // Local temp file has been created
                        Log.d("Firebase", "Download Success");

                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("*/*");
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Record");
                        Uri fileUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", localFile);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share via"));
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                        Log.e("Firebase", "Download Failed");
                    }
                });

                return true;
//                //Record record = (Record) parent.getItemAtPosition(position);
//                Intent shareIntent = new Intent(Intent.ACTION_SEND);
//                shareIntent.setType("text/plain");
//                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Share Record");
//                shareIntent.putExtra(Intent.EXTRA_TEXT, record.toString()); // Modify this based on how you want to represent the Record
//
//                startActivity(Intent.createChooser(shareIntent, "Share via"));
//                return true;
            }
        });

       return view;
    }

    private void loadRecordings() {
//        audioFiles = getAllFilesInExternalCache(getActivity());
//        for (File file : audioFiles) {
//            records.add(new Record(file.getName(), MP3Helper.getDurationFormatted(getActivity(), file), MP3Helper.getLastModifiedTime(getActivity(), file)));
//        }
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        FirebaseUser user = mAuth.getCurrentUser();

        SharedPreferences settings = getActivity().getSharedPreferences("LoginPrefs", 0);
        String name = settings.getString("username", "");
        String username;
        if (user != null) {
            username = user.getDisplayName();
        } else {
            username = name;
        }

        dbRef = FirebaseDatabase.getInstance().getReference("records");
        Log.d("Firebase", "Username: " + username);
        dbRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot recordSnapshot : dataSnapshot.getChildren()) {
                    RecordFirebase record = recordSnapshot.getValue(RecordFirebase.class);
                    records.add(new RecordFirebase(record.getRecordId(),record.getUsername(),record.getDownloadUrl(),record.getFileName(),record.getDuration()));
                    Log.d("Firebase", "Record: " + record.getFileName());
                }
                Log.d("RecordsFragment", "Records: " + records.size());
                adapter = new RecordAdapter(getActivity(), (ArrayList<RecordFirebase>) records);
                recordsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("Firebase", "Error getting data", error.toException());
            }
        });

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