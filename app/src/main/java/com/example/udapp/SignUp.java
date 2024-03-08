package com.example.udapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        TextView confirm = findViewById(R.id.confirm_password);

        //Sign up
        TextView signup = findViewById(R.id.signup_btn);
        signup.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            String conf = confirm.getText().toString();
            if (user.equals("") || pass.equals("") || conf.equals("")) {
                username.setError("Field cannot be empty");
                password.setError("Field cannot be empty");
                confirm.setError("Field cannot be empty");
            } else if (!pass.equals(conf)) {
                password.setError("Password does not match");
                confirm.setError("Password does not match");
            } else {
                username.setText("");
                password.setText("");
                confirm.setText("");
                username.setError(null);
                password.setError(null);
                confirm.setError(null);
                username.clearFocus();
                password.clearFocus();
                confirm.clearFocus();
                username.requestFocus();
                password.requestFocus();
                confirm.requestFocus();
            }
        });
    }
}