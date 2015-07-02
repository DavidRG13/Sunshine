package com.android.sunshine.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public DBHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {

        final String sqlCreateWeatherTable = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " ("
            + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, "
            + WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, "
            + WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
            + WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL,"
            + WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "
            + " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES "
            + LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), "
            + " UNIQUE (" + WeatherEntry.COLUMN_DATE + ", "
            + WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        final String sqlCreateLocationTable = "CREATE TABLE " + LocationEntry.TABLE_NAME + " ("
            + LocationEntry._ID + " INTEGER PRIMARY KEY, "
            + LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, "
            + LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, "
            + LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, "
            + LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, "
            + "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE"
            + ");";

        sqLiteDatabase.execSQL(sqlCreateWeatherTable);
        sqLiteDatabase.execSQL(sqlCreateLocationTable);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int oldVersion, final int newVersion) {
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
