package com.android.sunshine.app.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.android.sunshine.app.repository.WeatherContract.LocationEntry;
import static com.android.sunshine.app.repository.WeatherContract.WeatherEntry;

public class DBHelper extends SQLiteOpenHelper{

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";
    private static final String REAL_NOT_NULL = " REAL NOT NULL, ";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
                WeatherEntry.COLUMN_MIN_TEMP + REAL_NOT_NULL +
                WeatherEntry.COLUMN_MAX_TEMP + REAL_NOT_NULL +
                WeatherEntry.COLUMN_HUMIDITY + REAL_NOT_NULL +
                WeatherEntry.COLUMN_PRESSURE + REAL_NOT_NULL +
                WeatherEntry.COLUMN_WIND_SPEED + REAL_NOT_NULL +
                WeatherEntry.COLUMN_DEGREES + REAL_NOT_NULL +
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
                " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY, " +
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_COORD_LAT + REAL_NOT_NULL +
                LocationEntry.COLUMN_COORD_LONG + REAL_NOT_NULL +
                "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}