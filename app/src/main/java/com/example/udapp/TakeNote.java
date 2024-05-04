package com.example.udapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TakeNote extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_note);

        String fileName = getIntent().getStringExtra("fileName");
        String recordId = getIntent().getStringExtra("recordId");

        // Find the TextView and set the file name
        TextView recordName = findViewById(R.id.recordName);
        TextInputEditText notesTextView = findViewById(R.id.notes);

        String notes = getIntent().getStringExtra("notes");
        notesTextView.setText(notes);
        recordName.setText(fileName);

        Button save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(v -> {
            String notesText = notesTextView.getText().toString();
            // Save the notes to the database
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("records");
            dbRef.child(recordId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    RecordFirebase record = dataSnapshot.getValue(RecordFirebase.class);
                    if (record != null) {
                        record.setNotes(notesText);
                        dbRef.child(recordId).setValue(record);
                        Toast.makeText(TakeNote.this, "Note updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle possible errors.
                    Toast.makeText(TakeNote.this, "Error updating note", Toast.LENGTH_SHORT).show();
                }
            });

            finish();
        });
    }
}