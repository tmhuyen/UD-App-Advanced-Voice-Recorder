package com.example.udapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import org.mindrot.jbcrypt.BCrypt;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {
    private DatabaseReference dbRef;
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
            if(user.equals("")){
                username.setError("Field cannot be empty");
                if(pass.equals("")){
                    password.setError("Field cannot be empty");
                    if(conf.equals("")){
                        confirm.setError("Field cannot be empty");
                    }
                }
            }
            else if (!pass.equals(conf)) {
                password.setError("Password does not match");
                confirm.setError("Password does not match");
            } else {
                // Add user to database
//                MyDB db = new MyDB(this);
//                db.addUser(user, pass);
                dbRef = FirebaseDatabase.getInstance().getReference("users");
                String userId = dbRef.push().getKey();
                UserModel userModel = new UserModel(userId, user, pass);
                Log.d("SignUp", "UserModel: " + userModel.toString());
                Log.d("SignUp", "password" + userModel.getPassword());
                dbRef.child(userId).setValue(userModel)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                finish();

//                username.setText("");
//                password.setText("");
//                confirm.setText("");
//                username.setError(null);
//                password.setError(null);
//                confirm.setError(null);
//                username.clearFocus();
//                password.clearFocus();
//                confirm.clearFocus();
//                username.requestFocus();
//                password.requestFocus();
//                confirm.requestFocus();
            }
        });
    }
}