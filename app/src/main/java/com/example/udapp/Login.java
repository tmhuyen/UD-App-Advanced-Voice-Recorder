package com.example.udapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetCustomCredentialOption;
import androidx.credentials.PasswordCredential;
import androidx.credentials.PublicKeyCredential;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.credentials.GetCredentialException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Login extends AppCompatActivity{

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageView google_btn;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final int RC_SIGN_IN = 9001;
    private static final String SERVER_CLIENT_ID = "254965457831-dgmjq36p7gc572jp7t1crc20nmceh9ks.apps.googleusercontent.com";
    private GoogleSignInClient mGoogleSignInClient;
    private CredentialManager mCredentialManager;
    private Executor executor = Executors.newSingleThreadExecutor();
    private CancellationSignal cancellationSignal = new CancellationSignal();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Normal login
        TextView txtUsername = findViewById(R.id.username);
        TextView txtPassword = findViewById(R.id.password);

        // Check if user is already logged in
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (((SharedPreferences) settings).getString("logged", "").equals("logged")) {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Button login = findViewById(R.id.login_btn);
        login.setOnClickListener(v -> {
            String username = txtUsername.getText().toString();
            String password = txtPassword.getText().toString();

            if (username.equals("")) {
                txtUsername.setError("Field cannot be empty");
                if (password.equals("")) {
                    txtPassword.setError("Field cannot be empty");
                }
            }
            // Check if username and password correct
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference usersRef = database.getReference("users");

            usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            UserModel userModel = userSnapshot.getValue(UserModel.class);
                            if (userModel != null && userModel.checkPassword(password)) {
                                // Save login status
                                settings.edit().putString("logged", "logged").apply();

                                // Đăng nhập thành công, chuyển sang MainActivity
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Kết thúc hoạt động đăng nhập để không thể quay lại
                                return;
                            }
                        }
                        // Sai mật khẩu
                        txtPassword.setError("Invalid password");
                    } else {
                        // Sai tên người dùng
                        txtUsername.setError("Invalid username");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi
                    Log.e("LoginActivity", "onCancelled", error.toException());
                    Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });


            if (username.equals("admin") && password.equals("admin")) {
                txtUsername.setText("");
                txtPassword.setText("");
                txtUsername.setError(null);
                txtPassword.setError(null);
                txtUsername.clearFocus();
                txtPassword.clearFocus();
                txtUsername.requestFocus();
                txtPassword.requestFocus();
                Intent intent = new Intent(this, MainActivity.class);
            }

//            MyDB helper = new MyDB(this);
//            SQLiteDatabase db = helper.getWritableDatabase();
//            Cursor rs = db.rawQuery("SELECT * FROM USERS WHERE USERNAME LIKE ? AND PASSWORD LIKE ?", new String[]{user, pass});
//            if (rs!= null && rs.getCount() > 0) {
//                Intent intent = new Intent(this, MainActivity.class);
//                startActivity(intent);
//            }
        });

        //Sign up
        Button signup = findViewById(R.id.signup_btn);
        signup.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUp.class);
            startActivity(intent);
        });


    }
    GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(SERVER_CLIENT_ID)
            .build();

    GetCredentialRequest request = new GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build();
    private static final String TAG = "SignInWithGoogleActivity";


    // Set up a click listener for the google_btn  google_btn = findViewById(R.id.google);

}