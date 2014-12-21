package com.android.sunshine.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.android.sunshine.app.model.Contract;

public class DBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "articles.db";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + Contract.ArticleEntry.TABLE_NAME + " (" +
                Contract.ArticleEntry._ID + " TEXT PRIMARY KEY, " +
                Contract.ArticleEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                Contract.ArticleEntry.COLUMN_SHORT_DESCRIPTION + " TEXT NOT NULL, " +
                Contract.ArticleEntry.COLUMN_URL + " TEXT NOT NULL, " +
                Contract.ArticleEntry.COLUMN_SECTION_NAME + " TEXT NOT NULL, " +
                Contract.ArticleEntry.COLUMN_SNIPPET + " TEXT NOT NULL, " +
                Contract.ArticleEntry.COLUMN_THUMBNAIL + " TEXT, " +
                Contract.ArticleEntry.COLUMN_LARGE_IMAGE + " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + Contract.ArticleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}