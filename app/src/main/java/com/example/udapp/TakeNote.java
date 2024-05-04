package com.example.udapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TakeNote extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_note);

        String fileName = getIntent().getStringExtra("fileName");

        // Find the TextView and set the file name
        TextView recordName = findViewById(R.id.recordName);
        recordName.setText(fileName);
    }
}