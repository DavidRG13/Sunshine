package com.android.sunshine.app.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

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
        final ContentValues values = DbUtilities.createNorthPoleLocationValues();

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
        DbUtilities.validateCursor(cursor, values);
        dbHelper.close();
    }

    public void testInsertReadWeather() {
        resetDB();
        final DBHelper dbHelper = new DBHelper(mContext);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final long locationRowId = insertDefaultLocation(db);
        final ContentValues weatherValues = DbUtilities.createWeatherValues(locationRowId);

        final long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);

        final Cursor weatherCursor = db.query(WeatherEntry.TABLE_NAME, null, null, null, null, null, null);
        DbUtilities.validateCursor(weatherCursor, weatherValues);
        dbHelper.close();
    }

    private long insertDefaultLocation(final SQLiteDatabase db) {
        final long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, DbUtilities.createNorthPoleLocationValues());
        assertTrue(locationRowId != -1);
        return locationRowId;
    }

    private void resetDB() {
        DbUtilities.dropDb(getContext());
    }
}