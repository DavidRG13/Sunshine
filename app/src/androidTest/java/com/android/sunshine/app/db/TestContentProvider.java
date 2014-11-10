package com.android.sunshine.app.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class TestContentProvider extends AndroidTestCase {

    private DBHelper dbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(WeatherEntry.TABLE_NAME, null, null);
        db.delete(LocationEntry.TABLE_NAME, null, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        dbHelper.close();
    }

    public void testReadOneForecast() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        long locationRowId = insertLocation(testValues);
        ContentValues weatherValues = DbUtilities.createWeatherValues(locationRowId);
        insertWeather(weatherValues);

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI, null, null, null, null);

        DbUtilities.validateCursor(weatherCursor, weatherValues);
    }

    public void testFindForecastByLocation() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        long locationRowId = insertLocation(testValues);
        ContentValues weatherValues = DbUtilities.createWeatherValues(locationRowId);
        insertWeather(weatherValues);

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry
                .buildWeatherLocation(DbUtilities.DEFAULT_LOCATION_SETTINGS), null, null, null, null);
        DbUtilities.validateCursor(weatherCursor, weatherValues);
    }

    public void testFindForecastByLocationWithStartDate() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        long locationRowId = insertLocation(testValues);
        ContentValues weatherValues = DbUtilities.createWeatherValues(locationRowId);
        insertWeather(weatherValues);

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry
                        .buildWeatherLocationWithStartDate(DbUtilities.DEFAULT_LOCATION_SETTINGS, DbUtilities.DEFAULT_DATE),
                null, null, null, null);
        DbUtilities.validateCursor(weatherCursor, weatherValues);
    }

    public void testFindForecastByLocationWithStartDateShouldFailIfForecastDateGreaterThanPassed() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        long locationRowId = insertLocation(testValues);
        ContentValues weatherValues = DbUtilities.createWeatherValues(locationRowId);
        insertWeather(weatherValues);

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry
                        .buildWeatherLocationWithStartDate(DbUtilities.DEFAULT_LOCATION_SETTINGS, "20151205"),
                null, null, null, null);
        DbUtilities.validateCursor(weatherCursor, weatherValues);
    }

    public void testFindForecastByLocationWithDaySelection() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        long locationRowId = insertLocation(testValues);
        ContentValues weatherValues = DbUtilities.createWeatherValues(locationRowId);
        insertWeather(weatherValues);

        Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry
                        .buildWeatherLocationWithDate(DbUtilities.DEFAULT_LOCATION_SETTINGS, DbUtilities.DEFAULT_DATE),
                null, null, null, null);
        DbUtilities.validateCursor(weatherCursor, weatherValues);
    }

    public void testReadOneLocation() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        insertLocation(testValues);

        Cursor cursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI, null, null, null, null);

        DbUtilities.validateCursor(cursor, testValues);
    }

    public void testFindLocationById() {
        ContentValues testValues = DbUtilities.createNorthPoleLocationValues();
        long insertLocationId = insertLocation(testValues);

        Cursor cursor = mContext.getContentResolver().query(LocationEntry.buildLocationUri(insertLocationId), null, null, null, null);

        DbUtilities.validateCursor(cursor, testValues);
    }

    public void testGetType() {
        // content://com.example.android.sunshine.app/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.example.android.sunshine.app/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.example.android.sunshine.app/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.example.android.sunshine.app/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.example.android.sunshine.app/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.example.android.sunshine.app/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.example.android.sunshine.app/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    private long insertLocation(ContentValues contentValues) {
        final Uri uri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, contentValues);
        long locationRowId = ContentUris.parseId(uri);
        assertTrue(locationRowId != -1);
        return locationRowId;
    }

    private long insertWeather(ContentValues weatherValues) {
        final Uri uri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI, weatherValues);
        final long weatherRowId = ContentUris.parseId(uri);
        assertTrue(weatherRowId != -1);
        return weatherRowId;
    }
}