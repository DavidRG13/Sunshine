package com.android.sunshine.app.model;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class WeatherContract {

    public static final String CONTENT_AUTHORITY = "com.android.sunshine.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_WEATHER = "weather";
    public static final String DATE_FORMAT = "yyyyMMdd";

    private WeatherContract() { }

    public static String getDbDateString(final Date date) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static final String TABLE_NAME = "weather";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_WEATHER_ID = "weather_id";
        public static final String COLUMN_SHORT_DESC = "short_desc";
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";
        public static final String COLUMN_HUMIDITY = "humidity";
        public static final String COLUMN_PRESSURE = "pressure";
        public static final String COLUMN_WIND_SPEED = "wind";
        public static final String COLUMN_DEGREES = "degrees";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_CITY = "city";
        public static final String COLUMN_LOCATION_SETTINGS = "locSettings";

        public static Uri buildWeatherUri(final long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(final String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(final String locationSetting, final long startDate) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendQueryParameter(COLUMN_DATE, String.valueOf(startDate)).build();
        }

        public static Uri buildWeatherLocationWithDate(final String locationSetting, final long date) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(String.valueOf(date)).build();
        }

        public static String getLocationSettingFromUri(final Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getDateFromUri(final Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }

        public static String getStartDateFromUri(final Uri uri) {
            return uri.getQueryParameter(COLUMN_DATE);
        }
    }
}
