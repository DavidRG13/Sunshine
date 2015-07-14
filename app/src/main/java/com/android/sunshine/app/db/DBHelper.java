package com.android.sunshine.app.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";
    private static final int DATABASE_VERSION = 2;
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    public DBHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase sqLiteDatabase) {

        final String sqlCreateWeatherTable = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " ("
            + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, "
            + WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, "
            + WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL,"
            + WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_LATITUDE + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_LONGITUDE + " REAL NOT NULL, "
            + WeatherEntry.COLUMN_CITY + " TEXT NOT NULL, "
            + WeatherEntry.COLUMN_LOCATION_SETTINGS + " TEXT NOT NULL);";

        sqLiteDatabase.execSQL(sqlCreateWeatherTable);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqLiteDatabase, final int oldVersion, final int newVersion) {
        // TODO: remove this line in the next update
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + "location");
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
