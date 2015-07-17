package com.android.sunshine.app.db;

import android.provider.BaseColumns;

public final class WeatherTable implements BaseColumns {

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

    private WeatherTable() {
    }
}
