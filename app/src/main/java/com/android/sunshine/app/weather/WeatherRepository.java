package com.android.sunshine.app.weather;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import com.android.sunshine.app.R;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.model.OWMWeather;
import com.android.sunshine.app.model.OWMWeatherForecast;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.UserNotificator;
import com.android.sunshine.app.utils.WeatherNotification;
import com.android.sunshine.app.widget.ForecastDetailWidget;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.inject.Inject;

public class WeatherRepository {

    public static final String ACTION_DATA_UPDATED = "com.example.android.sunshine.app.ACTION_DATA_UPDATED";

    private static final String[] COLUMNS = new String[]{
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
        WeatherContract.WeatherEntry.COLUMN_DATE,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
        WeatherContract.WeatherEntry.COLUMN_PRESSURE,
        WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };
    private static final String[] FORECAST_COLUMNS = {
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID, WeatherContract.WeatherEntry.COLUMN_DATE, WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_WEATHER_DATE = 1;
    private static final int INDEX_WEATHER_CONDITION_ID = 2;
    private static final int INDEX_WEATHER_DESC = 3;
    private static final int INDEX_WEATHER_MAX_TEMP = 4;
    private static final int INDEX_WEATHER_MIN_TEMP = 5;

    private final Context context;
    private final UserNotificator userNotificator;
    private final LocationProvider locationProvider;

    private TemperatureFormatter temperatureFormatter;
    private ContentResolver contentResolver;
    private DateFormatter dateFormatter;

    @Inject
    public WeatherRepository(final Context context, final UserNotificator userNotificator, final LocationProvider locationProvider, 
        final TemperatureFormatter temperatureFormatter, final ContentResolver contentResolver, final DateFormatter dateFormatter) {
        this.context = context;
        this.userNotificator = userNotificator;
        this.locationProvider = locationProvider;
        this.temperatureFormatter = temperatureFormatter;
        this.contentResolver = contentResolver;
        this.dateFormatter = dateFormatter;
    }

    public void saveWeatherForLocation(final OWMResponse owmResponse, final String locationSettings) {
        String cityName = owmResponse.getCity().getName();
        double cityLatitude = owmResponse.getCity().getCoord().getLat();
        double cityLongitude = owmResponse.getCity().getCoord().getLon();

        long locationId = addLocation(locationSettings, cityName, cityLatitude, cityLongitude);

        ArrayList<OWMWeatherForecast> weatherForecasts = owmResponse.getList();
        if (!weatherForecasts.isEmpty()) {
            ContentValues[] contentValues = toContentValues(locationId, weatherForecasts);

            contentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);
            userNotificator.notifyWeather(weatherForecasts.get(0));
            updateWidgets();

            removePastForecast();
        }
    }

    public WeatherNotification getForecastFor(final long date, final String location) {
        final Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, date);
        Cursor cursor = contentResolver.query(weatherUri, COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        WeatherNotification weatherNotification = WeatherNotification.INVALID_OBJECT;
        if (cursor.moveToFirst()) {
            final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            final String description = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            final long forecastDate = cursor.getLong(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
            final String wind = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            final String pressure = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));
            final String humidity = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
            final double maxTemp = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            final double minTemp = cursor.getDouble(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            final String max = temperatureFormatter.format(maxTemp);
            final String min = temperatureFormatter.format(minTemp);

            weatherNotification = new WeatherNotification(weatherId, description, forecastDate, wind, pressure, humidity, max, min);
        }
        cursor.close();
        return weatherNotification;
    }

    private void removePastForecast() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterdayDate = WeatherContract.getDbDateString(cal.getTime());
        contentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?", new String[] {yesterdayDate});
    }

    private void updateWidgets() {
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private long addLocation(final String locationSettings, final String cityName, final double cityLatitude, final double cityLongitude) {
        locationProvider.saveLocation(cityLatitude, cityLongitude);
        ContentValues locationValues = new ContentValues();
        locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSettings);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, cityLatitude);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, cityLongitude);

        long result;
        final Cursor cursor = contentResolver.query(WeatherContract.LocationEntry.CONTENT_URI, new String[] {WeatherContract.LocationEntry._ID },
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?", new String[] {locationSettings}, null);
        if (cursor.moveToFirst()) {
            result = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
        } else {
            result = ContentUris.parseId(contentResolver.insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues));
        }
        cursor.close();
        return result;
    }

    private ContentValues[] toContentValues(final long locationId, final ArrayList<OWMWeatherForecast> weatherForecasts) {
        ContentValues[] contentValues = new ContentValues[weatherForecasts.size()];
        GregorianCalendar calendar = new GregorianCalendar();
        for (int i = 0; i < weatherForecasts.size(); i++) {
            OWMWeatherForecast weatherForecast = weatherForecasts.get(i);
            ContentValues weatherValues = new ContentValues();
            if (i > 0) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, calendar.getTimeInMillis());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, weatherForecast.getHumidity());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, weatherForecast.getPressure());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, weatherForecast.getSpeed());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, weatherForecast.getDeg());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, weatherForecast.getTemp().getMax());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, weatherForecast.getTemp().getMin());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, weatherForecast.getWeather().get(0).getDescription());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherForecast.getWeather().get(0).getId());

            contentValues[i] = weatherValues;
        }
        return contentValues;
    }

    public ArrayList<ForecastDetailWidget> getForecastForDetailWidget() {
        ArrayList<ForecastDetailWidget> forecasts = new ArrayList<>();
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationProvider.getPostCode(), System.currentTimeMillis());
        Cursor data = contentResolver.query(weatherForLocationUri, FORECAST_COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        while (data.moveToNext()) {
            int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
            int weatherArtResourceId = OWMWeather.getIconResourceForWeatherCondition(weatherId);
            String description = data.getString(INDEX_WEATHER_DESC);
            long dateInMillis = data.getLong(INDEX_WEATHER_DATE);
            String formattedDate = dateFormatter.getFriendlyDay(dateInMillis, false);
            double maxTemp = data.getDouble(INDEX_WEATHER_MAX_TEMP);
            double minTemp = data.getDouble(INDEX_WEATHER_MIN_TEMP);
            forecasts.add(new ForecastDetailWidget(weatherId, weatherArtResourceId, description, dateInMillis, formattedDate, temperatureFormatter.format(maxTemp), temperatureFormatter.format(minTemp), locationProvider.getPostCode()));
        }
        data.close();
        return forecasts;
    }
}
