package com.android.sunshine.app.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import com.android.sunshine.app.model.WeatherContract;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final UriMatcher URI_MATCHER = buildUriMatcher();
    private static final String LOCATION_SETTINGS_SELECTION = WeatherEntry.COLUMN_LOCATION_SETTINGS + " = ?";
    private static final String LOCATION_SETTINGS_WITH_START_DATE_SELECTION = WeatherEntry.COLUMN_LOCATION_SETTINGS + " = ? AND " + WeatherEntry.COLUMN_DATE + " >= ? ";
    private static final String LOCATION_SETTINGS_WITH_DAY_SELECTION = WeatherEntry.COLUMN_LOCATION_SETTINGS + " = ? AND " + WeatherEntry.COLUMN_DATE + " = ? ";
    private static SQLiteQueryBuilder weatherByLocationSettingsQueryBuilder;

    static {
        weatherByLocationSettingsQueryBuilder = new SQLiteQueryBuilder();
        weatherByLocationSettingsQueryBuilder.setTables(WeatherEntry.TABLE_NAME);
    }

    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        db = new DBHelper(getContext()).getWritableDatabase();
        return true;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder) {
        final Cursor cursor;
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                cursor = db.query(WeatherEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
                cursor = getWeatherByLocationSettings(uri, projection, sortOrder);
                break;
            case WEATHER_WITH_LOCATION_AND_DATE:
                cursor = getWeatherByLocationSettingsWithDate(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(final Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues contentValues) {
        final Uri returnUri;
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                returnUri = insertInTo(WeatherEntry.TABLE_NAME, contentValues, uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(final Uri uri, final ContentValues[] values) {
        final int count;
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                count = bulkInsertIn(WeatherEntry.TABLE_NAME, values);
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(final Uri uri, final String whereClause, final String[] whereArgs) {
        final int rowsDeleted;
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                rowsDeleted = db.delete(WeatherEntry.TABLE_NAME, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (whereClause == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(final Uri uri, final ContentValues contentValues, final String whereClause, final String[] whereArgs) {
        final int rowsUpdated;
        switch (URI_MATCHER.match(uri)) {
            case WEATHER:
                rowsUpdated = db.update(WeatherEntry.TABLE_NAME, contentValues, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private Uri insertInTo(final String tableName, final ContentValues contentValues, final Uri uri) {
        final long insertedId = db.insert(tableName, null, contentValues);
        if (insertedId > 0) {
            return WeatherEntry.buildWeatherUri(insertedId);
        } else {
            throw new SQLException("Failed to insert row into " + uri);
        }
    }

    private int bulkInsertIn(final String tableName, final ContentValues[] values) {
        db.beginTransaction();
        int count = 0;
        try {
            for (ContentValues value : values) {
                final long insertedId = db.insert(tableName, null, value);
                if (insertedId > 0) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return count;
    }

    private Cursor getWeatherByLocationSettings(final Uri uri, final String[] projection, final String sortOrder) {
        final String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        final String startDate = WeatherEntry.getStartDateFromUri(uri);
        String[] selectionArgs;
        String selection;
        if (startDate == null) {
            selection = LOCATION_SETTINGS_SELECTION;
            selectionArgs = new String[]{locationSetting};
        } else {
            selection = LOCATION_SETTINGS_WITH_START_DATE_SELECTION;
            selectionArgs = new String[]{locationSetting, startDate};
        }
        return weatherByLocationSettingsQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
    }

    private Cursor getWeatherByLocationSettingsWithDate(final Uri uri, final String[] projection, final String sortOrder) {
        final String locationSetting = WeatherEntry.getLocationSettingFromUri(uri);
        final long startDate = WeatherEntry.getDateFromUri(uri);
        return weatherByLocationSettingsQueryBuilder.query(
            db,
            projection,
            LOCATION_SETTINGS_WITH_DAY_SELECTION,
            new String[]{locationSetting, String.valueOf(startDate) },
            null,
            null,
            sortOrder);
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = WeatherContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        return matcher;
    }
}
