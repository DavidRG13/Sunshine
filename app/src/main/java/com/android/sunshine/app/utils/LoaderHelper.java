package com.android.sunshine.app.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.WeatherContract;
import java.util.Date;
import javax.inject.Inject;

public class LoaderHelper {

    public static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = new String[] {
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID, WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, WeatherContract.LocationEntry.COLUMN_COORD_LAT, WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    private LocationProvider locationProvider;

    @Inject
    public LoaderHelper(final LocationProvider locationProvider) {
        this.locationProvider = locationProvider;
    }

    public Loader<Cursor> getForecastCursorLoader(final Context context) {
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationProvider.getPostCode(), new Date().getTime());

        return new CursorLoader(context, weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    public int getForecastLoaderId() {
        return FORECAST_LOADER;
    }
}
