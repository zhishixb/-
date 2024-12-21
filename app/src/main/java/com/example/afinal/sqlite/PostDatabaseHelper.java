package com.example.afinal.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PostDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "posts.db";
    private static final int DATABASE_VERSION = 1;

    // SQL statement to create the posts table
    private static final String TABLE_CREATE =
            "CREATE TABLE posts (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + // id (auto-increment, primary key)
                    "uid TEXT NOT NULL, " +                    // user id
                    "title TEXT, " +                           // title
                    "content TEXT, " +                         // content
                    "img TEXT, " +                             // image name string
                    "time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" + // time of entry creation
                    ");";

    public PostDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if necessary
        db.execSQL("DROP TABLE IF EXISTS posts");
        onCreate(db);
    }
}