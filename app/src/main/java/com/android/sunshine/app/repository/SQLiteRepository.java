package com.android.sunshine.app.repository;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.android.sunshine.app.repository.WeatherContract.LocationEntry;
import com.android.sunshine.app.repository.WeatherContract.WeatherEntry;
import com.android.sunshine.app.sync.WeatherDataSource;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.LocationProperties;
import com.android.sunshine.app.utils.Weather;
import com.android.sunshine.app.utils.WeatherJsonParser;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import javax.inject.Inject;

public class SQLiteRepository implements ForecastRepository {

    private Context context;
    private DateFormatter dateFormatter;
    private final WeatherDataSource weatherDataSource;
    private final WeatherJsonParser weatherJsonParser;

    @Inject
    public SQLiteRepository(final Context context, final DateFormatter dateFormatter, final WeatherDataSource weatherDataSource, final WeatherJsonParser weatherJsonParser) {
        this.context = context;
        this.dateFormatter = dateFormatter;
        this.weatherDataSource = weatherDataSource;
        this.weatherJsonParser = weatherJsonParser;
    }

    @Override
    public boolean fetchForecast(final String location) {
        String jsonResponse = weatherDataSource.getForecastFor(location);
        LocationProperties parsedLocation = weatherJsonParser.parseLocation(jsonResponse, location);
        ArrayList<Weather> weathers = weatherJsonParser.parseWeatherDataFromJson(jsonResponse, addLocation(parsedLocation));
        return saveWeathers(weathers) > 0;
    }

    @Override
    public int saveWeathers(final List<Weather> weathers) {
        int inserted = 0;
        if (weathers.size() > 0) {
            deletePreviousData();
            inserted = context.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, convertToContentValues(weathers));
        }
        return inserted;
    }

    @Override
    public long addLocation(final LocationProperties locationProperties) {
        ContentValues locationValues = new ContentValues();
        String locationSettings = locationProperties.getLocationSettings();
        locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSettings);
        locationValues.put(LocationEntry.COLUMN_CITY_NAME, locationProperties.getCityName());
        locationValues.put(LocationEntry.COLUMN_COORD_LAT, locationProperties.getCityLatitude());
        locationValues.put(LocationEntry.COLUMN_COORD_LONG, locationProperties.getCityLongitude());

        final Cursor cursor = context.getContentResolver().query(LocationEntry.CONTENT_URI, new String[]{ LocationEntry._ID},
            LocationEntry.COLUMN_LOCATION_SETTING + " = ?", new String[]{locationSettings}, null);
        if (cursor.moveToFirst()) {
            return cursor.getColumnIndex(LocationEntry._ID);
        } else {
            return ContentUris.parseId(context.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues));
        }
    }

    private void deletePreviousData() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterdayDate = dateFormatter.getDbDateString(cal.getTime());
        context.getContentResolver().delete(WeatherEntry.CONTENT_URI, WeatherEntry.COLUMN_DATETEXT + " <= ?", new String[] { yesterdayDate });
    }

    private ContentValues[] convertToContentValues(final List<Weather> weathers) {
        Vector<ContentValues> vector = new Vector<>(weathers.size());
        for (Weather weather : weathers) {
            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, weather.getLocationId());
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, weather.getDate());
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, weather.getHumidity());
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, weather.getPressure());
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, weather.getWindSpeed());
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, weather.getWindDirection());
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, weather.getHighTemp());
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, weather.getLowTemp());
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, weather.getDescription());
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weather.getId());

            vector.add(weatherValues);
        }
        ContentValues[] contentValues = new ContentValues[vector.size()];
        vector.toArray(contentValues);
        return contentValues;
    }
}
