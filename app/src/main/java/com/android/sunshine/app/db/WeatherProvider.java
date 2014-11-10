package com.android.sunshine.app.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.android.sunshine.app.model.WeatherContract;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static final String locationSettingsSelection = LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ?";
    private static final String locationSettingsWithStartDateSelection = LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " + WeatherEntry.COLUMN_DATETEXT + " >= ? ";
    private static final String locationSettingsWithDaySelection = LocationEntry.TABLE_NAME + "." +
            LocationEntry.COLUMN_LOCATION_SETTING + " = ? AND " + WeatherEntry.COLUMN_DATETEXT + " = ? ";
    private DBHelper dbHelper;

    private static SQLiteQueryBuilder weatherByLocationSettingsQueryBuilder;

    static {
        weatherByLocationSettingsQueryBuilder = new SQLiteQueryBuilder();
        weatherByLocationSettingsQueryBuilder.setTables(
                WeatherEntry.TABLE_NAME + " INNER JOIN " + LocationEntry.TABLE_NAME + " ON " +
                        WeatherEntry.TABLE_NAME + "." + WeatherEntry.COLUMN_LOC_KEY + " = " +
                        LocationEntry.TABLE_NAME + "." + LocationEntry._ID);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case WEATHER:
                cursor = dbHelper.getReadableDatabase().query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
                cursor = getWeatherByLocationSettings(uri, projection, sortOrder);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                cursor = getWeatherByLocationSettingsWithDate(uri, projection, sortOrder);
                break;
            case LOCATION:
                cursor = dbHelper.getReadableDatabase().query(LocationEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case LOCATION_ID:
                cursor = dbHelper.getReadableDatabase().query(LocationEntry.TABLE_NAME, projection,
                        LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'", selectionArgs, null,
                        null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return LocationEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        final Uri returnUri;
        switch (match) {
            case WEATHER:
                final long insertedId = db.insert(WeatherEntry.TABLE_NAME, null, contentValues);
                if (insertedId > 0) {
                    returnUri = WeatherEntry.buildWeatherUri(insertedId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            case LOCATION:
                final long insertedLocationId = db.insert(LocationEntry.TABLE_NAME, null, contentValues);
                if (insertedLocationId > 0) {
                    returnUri = LocationEntry.buildLocationUri(insertedLocationId);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for (ContentValues value : values) {
                        final long insertedId = db.insert(WeatherEntry.TABLE_NAME, null, value);
                        if(insertedId > 0){
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case LOCATION:
                db.beginTransaction();
                int returnCountWeather = 0;
                try{
                    for (ContentValues value : values) {
                        final long insertedId = db.insert(LocationEntry.TABLE_NAME, null, value);
                        if(insertedId > 0){
                            returnCountWeather++;
                        }
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCountWeather;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String whereClause, String[] whereArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        final int rowsDeleted;
        switch (match) {
            case WEATHER:
                rowsDeleted = db.delete(WeatherEntry.TABLE_NAME, whereClause, whereArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(LocationEntry.TABLE_NAME, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(whereClause == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        final int rowsUpdated;
        switch (match) {
            case WEATHER:
                rowsUpdated = db.update(WeatherEntry.TABLE_NAME, contentValues, whereClause, whereArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(LocationEntry.TABLE_NAME, contentValues, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private Cursor getWeatherByLocationSettings(Uri uri, String[] projection, String sortOrder) {
        final String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        final String startDate = WeatherEntry.getStartDateFromUri(uri);
        String[] selectionArgs;
        String selection;
        if (startDate == null) {
            selection = locationSettingsSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selection = locationSettingsWithStartDateSelection;
            selectionArgs = new String[]{locationSetting, startDate};
        }
        return weatherByLocationSettingsQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getWeatherByLocationSettingsWithDate(Uri uri, String[] projection, String sortOrder) {
        final String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        final String startDate = WeatherEntry.getDateFromUri(uri);
        return weatherByLocationSettingsQueryBuilder.query(dbHelper.getReadableDatabase(),
                projection, locationSettingsWithDaySelection, new String[]{locationSetting, startDate},
                null, null, sortOrder);
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
        matcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);

        return matcher;
    }
}