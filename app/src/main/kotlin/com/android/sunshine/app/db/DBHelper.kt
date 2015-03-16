package com.android.sunshine.app.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.android.sunshine.app.model.WeatherContract.LocationEntry
import com.android.sunshine.app.model.WeatherContract.WeatherEntry

public class DBHelper(context: Context) : SQLiteOpenHelper(context, DBHelper.DATABASE_NAME, null, DBHelper.DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

        val SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" + 
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
                WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " + 
                WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " + 
                WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " + 
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," + 
                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " + 
                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " + 
                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " + 
                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " + 
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " + 
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " + 
                " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " + 
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " + 
                " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " + 
                WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);"

        val SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" + 
                LocationEntry._ID + " INTEGER PRIMARY KEY, " + 
                LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " + 
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " + 
                LocationEntry.COLUMN_COORD_LAT + " REAL NOT NULL, " + 
                LocationEntry.COLUMN_COORD_LONG + " REAL NOT NULL, " + 
                "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE" + ");"

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE)
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + LocationEntry.TABLE_NAME)
        sqLiteDatabase.execSQL(DROP_TABLE_IF_EXISTS + WeatherEntry.TABLE_NAME)
        onCreate(sqLiteDatabase)
    }

    class object {

        private val DATABASE_VERSION = 1
        public val DATABASE_NAME: String = "weather.db"
        private val DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS "
    }
}