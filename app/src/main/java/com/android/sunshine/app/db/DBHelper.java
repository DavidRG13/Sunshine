package com.android.sunshine.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 4;
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public DBHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {

        final String sqlCreateWeatherTable = "CREATE TABLE " + WeatherTable.TABLE_NAME + " ("
            + WeatherTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherTable.COLUMN_DATE + " INTEGER NOT NULL, "
            + WeatherTable.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
            + WeatherTable.COLUMN_WEATHER_ID + " INTEGER NOT NULL,"
            + WeatherTable.COLUMN_MIN_TEMP + " REAL NOT NULL, "
            + WeatherTable.COLUMN_MAX_TEMP + " REAL NOT NULL, "
            + WeatherTable.COLUMN_HUMIDITY + " REAL NOT NULL, "
            + WeatherTable.COLUMN_PRESSURE + " REAL NOT NULL, "
            + WeatherTable.COLUMN_WIND_SPEED + " REAL NOT NULL, "
            + WeatherTable.COLUMN_DEGREES + " REAL NOT NULL, "
            + WeatherTable.COLUMN_LATITUDE + " REAL NOT NULL, "
            + WeatherTable.COLUMN_LONGITUDE + " REAL NOT NULL, "
            + WeatherTable.COLUMN_CITY + " TEXT NOT NULL, "
            + WeatherTable.COLUMN_LOCATION_SETTINGS + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(sqlCreateWeatherTable);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int oldVersion, final int newVersion) {
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + WeatherTable.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
