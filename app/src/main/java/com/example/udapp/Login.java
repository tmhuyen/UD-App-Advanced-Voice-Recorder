package com.example.udapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class Login extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView google_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Normal login
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);

        Button login = findViewById(R.id.login_btn);
        login.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            if (user.equals("") || pass.equals("")) {
                username.setError("Field cannot be empty");
                password.setError("Field cannot be empty");
            }
            else if (user.equals("admin") && pass.equals("admin")) {
                username.setText("");
                password.setText("");
                username.setError(null);
                password.setError(null);
                username.clearFocus();
                password.clearFocus();
                username.requestFocus();
                password.requestFocus();
            } else {
                username.setError("Invalid username or password");
                password.setError("Invalid username or password");
            }
        });

        //Sign up
        Button signup = findViewById(R.id.signup_btn);
        signup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUp.class);
            startActivity(intent);
        });
        //Login with google
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        google_btn = findViewById(R.id.google);
        google_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = gsc.getSignInIntent();
                startActivityForResult(signInIntent, 1000);
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task =  GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                //navigateToSecondActivity();
            } catch (ApiException e) {
                Toast.makeText(getApplicationContext(),"Something went wrong!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*private void navigateToSecondActivity() {
        finish();
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }*/
}