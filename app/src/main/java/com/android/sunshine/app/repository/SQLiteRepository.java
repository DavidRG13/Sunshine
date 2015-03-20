package com.android.sunshine.app.repository;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.LocationProperties;
import com.android.sunshine.app.utils.Weather;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
import javax.inject.Inject;

public class SQLiteRepository implements ForecastRepository {

    private Context context;
    private DateFormatter dateFormatter;

    @Inject
    public SQLiteRepository(final Context context, final DateFormatter dateFormatter) {
        this.context = context;
        this.dateFormatter = dateFormatter;
    }

    @Override
    public void saveWeathers(final List<Weather> weathers) {
        if (weathers.size() > 0) {
            context.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, convertToContentValues(weathers));

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdayDate = dateFormatter.getDbDateString(cal.getTime());
            context.getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT + " <= ?", new String[] {yesterdayDate});
        }
    }

    @Override
    public long addLocation(final LocationProperties locationProperties) {
        ContentValues locationValues = new ContentValues();
        String locationSettings = locationProperties.getLocationSettings();
        locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSettings);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, locationProperties.getCityName());
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, locationProperties.getCityLatitude());
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, locationProperties.getCityLongitude());

        final Cursor cursor = context.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI, new String[]{WeatherContract.LocationEntry._ID},
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?", new String[]{locationSettings}, null);
        if (cursor.moveToFirst()) {
            return cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
        } else {
            return ContentUris.parseId(context.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues));
        }
    }

    private ContentValues[] convertToContentValues(final List<Weather> weathers) {
        Vector<ContentValues> vector = new Vector<>(weathers.size());
        for (Weather weather : weathers) {
            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, weather.getLocationId());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, weather.getDate());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, weather.getHumidity());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, weather.getPressure());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, weather.getWindSpeed());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, weather.getWindDirection());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, weather.getHighTemp());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, weather.getLowTemp());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, weather.getDescription());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weather.getId());

            vector.add(weatherValues);
        }
        ContentValues[] contentValues = new ContentValues[vector.size()];
        vector.toArray(contentValues);
        return contentValues;
    }
}
