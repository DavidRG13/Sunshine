package com.android.sunshine.app.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.android.sunshine.app.model.Contract;

import static com.android.sunshine.app.model.Contract.ArticleEntry;

public class WeatherProvider extends ContentProvider {

    private static final int ARTICLE = 100;
    private static final int ARTICLES_WITH_START_DATE = 200;
    private static final UriMatcher uriMatcher = buildUriMatcher();
    private DBHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DBHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case ARTICLE:
                cursor = dbHelper.getReadableDatabase().query(ArticleEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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
            case ARTICLE:
                return ArticleEntry.CONTENT_TYPE;
            case ARTICLES_WITH_START_DATE:
                return ArticleEntry.CONTENT_TYPE;
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
            case ARTICLE:
                final long insertedId = db.insert(ArticleEntry.TABLE_NAME, null, contentValues);
                if (insertedId > 0) {
                    returnUri = ArticleEntry.buildWeatherUri(insertedId);
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
            case ARTICLE:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for (ContentValues value : values) {
                        final long insertedId = db.insertWithOnConflict(ArticleEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
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
            case ARTICLE:
                rowsDeleted = db.delete(ArticleEntry.TABLE_NAME, whereClause, whereArgs);
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
            case ARTICLE:
                rowsUpdated = db.update(ArticleEntry.TABLE_NAME, contentValues, whereClause, whereArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        matcher.addURI(authority, Contract.PATH_ARTICLES, ARTICLE);
        matcher.addURI(authority, Contract.PATH_ARTICLES + "/*", ARTICLES_WITH_START_DATE);

        return matcher;
    }
}