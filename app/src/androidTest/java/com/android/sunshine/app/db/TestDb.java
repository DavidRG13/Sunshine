package com.android.sunshine.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class TestDb extends AndroidTestCase {

    public void testCreateDb() throws Throwable {
        resetDB();
        final SQLiteDatabase db = new DBHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadLocation() {
        resetDB();
        final DBHelper dbHelper = new DBHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final ContentValues values = createNorthPoleLocationValues();

        final long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, values);
        assertTrue(locationRowId != -1);

        final String[] columns = {
                LocationEntry._ID,
                LocationEntry.COLUMN_LOCATION_SETTING,
                LocationEntry.COLUMN_CITY_NAME,
                LocationEntry.COLUMN_COORD_LAT,
                LocationEntry.COLUMN_COORD_LONG
        };

        final Cursor cursor = db.query(LocationEntry.TABLE_NAME, columns, null, null, null, null, null);
        validateCursor(cursor, values);
        dbHelper.close();
    }

    public void testInsertReadWeather() {
        resetDB();
        final DBHelper dbHelper = new DBHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final long locationRowId = insertDefaultLocation(db);
        final ContentValues weatherValues = createWeatherValues(locationRowId);

        final long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        final Cursor weatherCursor = db.query(WeatherEntry.TABLE_NAME, null, null, null, null, null, null);
        validateCursor(weatherCursor, weatherValues);
        dbHelper.close();
    }

    private long insertDefaultLocation(final SQLiteDatabase db) {
        final long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, createNorthPoleLocationValues());
        assertTrue(locationRowId != -1);
        return locationRowId;
    }

    private void resetDB() {
        mContext.deleteDatabase(DBHelper.DATABASE_NAME);
    }

    private static ContentValues createWeatherValues(final long locationRowId) {
        final ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        return weatherValues;
    }

    private static ContentValues createNorthPoleLocationValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(LocationEntry.COLUMN_LOCATION_SETTING, "99705");
        testValues.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        testValues.put(LocationEntry.COLUMN_COORD_LAT, 64.7488);
        testValues.put(LocationEntry.COLUMN_COORD_LONG, -147.353);

        return testValues;
    }

    private static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }
}