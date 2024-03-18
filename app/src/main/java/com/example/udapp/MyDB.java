package com.example.udapp;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDB extends SQLiteOpenHelper {

    public MyDB(Context context) {
        super(context, "USERDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL("CREATE TABLE USERS(USERID INTEGER PRIMARY KEY AUTOINCREMENT, USERNAME TEXT, PASSWORD TEXT)");

        // add data
        db.execSQL("INSERT INTO USERS(USERNAME, PASSWORD) VALUES('thduy', 'abc123')");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if necessary
    }

    public void addUser(String user, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("INSERT INTO USERS(USERNAME, PASSWORD) VALUES('" + user + "', '" + pass + "')");

    }
}