package com.android.sunshine.app.weather;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.model.OWMWeather;
import com.android.sunshine.app.model.OWMWeatherForecast;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.ApplicationPreferences;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.WeatherNotification;
import com.android.sunshine.app.widget.ForecastDetailWidget;
import com.android.sunshine.app.widget.TodayForecast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SQLiteDataSource implements WeatherDataSource {

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

    private static final String[] TODAY_WIDGET_FORECAST_COLUMNS = {
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    private static final String[] FORECAST_LIST_COLUMNS = new String[] {
        WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
        WeatherContract.WeatherEntry.COLUMN_DATE,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    private final ContentResolver contentResolver;
    private final DateFormatter dateFormatter;
    private LocationProvider locationProvider;
    private ApplicationPreferences applicationPreferences;

    @Inject
    public SQLiteDataSource(final ContentResolver contentResolver, final DateFormatter dateFormatter, final LocationProvider locationProvider,
        final ApplicationPreferences applicationPreferences) {
        this.contentResolver = contentResolver;
        this.dateFormatter = dateFormatter;
        this.locationProvider = locationProvider;
        this.applicationPreferences = applicationPreferences;
    }

    @Override
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
            final String max = applicationPreferences.getTemperatureUnit().format(maxTemp);
            final String min = applicationPreferences.getTemperatureUnit().format(minTemp);

            weatherNotification = new WeatherNotification(weatherId, description, forecastDate, wind, pressure, humidity, max, min);
        }
        cursor.close();
        return weatherNotification;
    }

    @Override
    public ArrayList<ForecastDetailWidget> getForecastForDetailWidget() {
        ArrayList<ForecastDetailWidget> forecasts = new ArrayList<>();
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationProvider.getPostCode(), System.currentTimeMillis());
        Cursor data = contentResolver.query(weatherForLocationUri, FORECAST_LIST_COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        while (data.moveToNext()) {
            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            int weatherArtResourceId = OWMWeather.getIconResourceForWeatherCondition(weatherId);
            String description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            long dateInMillis = data.getLong(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
            String formattedDate = dateFormatter.getFriendlyDay(dateInMillis, false);
            double maxTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            double minTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            forecasts.add(new ForecastDetailWidget(weatherId, weatherArtResourceId, description, dateInMillis, formattedDate,
                applicationPreferences.getTemperatureUnit().format(maxTemp), applicationPreferences.getTemperatureUnit().format(minTemp),
                locationProvider.getPostCode()));
        }
        data.close();
        return forecasts;
    }

    @Override
    public List<ForecastFragmentWeather> getForecastList() {
        final ArrayList<ForecastFragmentWeather> forecastList = new ArrayList<>();

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationProvider.getPostCode(), new Date().getTime());

        Cursor cursor = contentResolver.query(weatherForLocationUri, FORECAST_LIST_COLUMNS, null, null, sortOrder);
        while (cursor.moveToNext()) {
            final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            final long weatherDate = cursor.getLong(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
            final String descriptionWeather = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            final float maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            final float minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            forecastList.add(new ForecastFragmentWeather(weatherId, weatherDate, dateFormatter.getFriendlyDay(weatherDate, true),
                dateFormatter.getFriendlyDay(weatherDate, false), descriptionWeather,
                applicationPreferences.getTemperatureUnit().format(maxTemp), applicationPreferences.getTemperatureUnit().format(minTemp)));
        }
        cursor.close();
        return forecastList;
    }

    @Override
    public TodayForecast getForecastForNowAndCurrentPosition() {
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationProvider.getPostCode(), System.currentTimeMillis());
        Cursor data = contentResolver.query(weatherForLocationUri, TODAY_WIDGET_FORECAST_COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        TodayForecast todayForecast = TodayForecast.INVALID_OBJECT;
        if (data.moveToFirst()) {
            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            int weatherArtResourceId = OWMWeather.getArtResourceForWeatherCondition(weatherId);
            String description = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC));
            double maxTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            double minTemp = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
            String formattedMaxTemperature = applicationPreferences.getTemperatureUnit().format(maxTemp);
            String formattedMinTemperature = applicationPreferences.getTemperatureUnit().format(minTemp);
            todayForecast = new TodayForecast(weatherArtResourceId, description, formattedMaxTemperature, formattedMinTemperature);
        }
        data.close();
        return todayForecast;
    }

    @Override
    public void saveWeatherForLocation(final OWMResponse owmResponse, final String locationSettings) {
        String cityName = owmResponse.getCity().getName();
        double cityLatitude = owmResponse.getCity().getCoord().getLat();
        double cityLongitude = owmResponse.getCity().getCoord().getLon();

        ArrayList<OWMWeatherForecast> weatherForecasts = owmResponse.getList();
        if (!weatherForecasts.isEmpty()) {
            ContentValues[] contentValues = toContentValues(weatherForecasts, cityName, cityLatitude, cityLongitude, locationSettings);

            contentResolver.bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, contentValues);


            removePastForecast();
        }
    }

    private ContentValues[] toContentValues(final ArrayList<OWMWeatherForecast> weatherForecasts, final String cityName, final double cityLatitude,
        final double cityLongitude, final String locationSettings) {
        ContentValues[] contentValues = new ContentValues[weatherForecasts.size()];
        GregorianCalendar calendar = new GregorianCalendar();
        for (int i = 0; i < weatherForecasts.size(); i++) {
            OWMWeatherForecast weatherForecast = weatherForecasts.get(i);
            ContentValues weatherValues = new ContentValues();
            if (i > 0) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, calendar.getTimeInMillis());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, weatherForecast.getHumidity());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, weatherForecast.getPressure());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, weatherForecast.getSpeed());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, weatherForecast.getDeg());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, weatherForecast.getTemp().getMax());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, weatherForecast.getTemp().getMin());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, weatherForecast.getWeather().get(0).getDescription());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherForecast.getWeather().get(0).getId());
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_CITY, cityName);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LATITUDE, cityLatitude);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LONGITUDE, cityLongitude);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOCATION_SETTINGS, locationSettings);

            contentValues[i] = weatherValues;
        }
        return contentValues;
    }

    private void removePastForecast() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        String yesterdayDate = WeatherContract.getDbDateString(cal.getTime());
        contentResolver.delete(WeatherContract.WeatherEntry.CONTENT_URI, WeatherContract.WeatherEntry.COLUMN_DATE + " <= ?", new String[] {yesterdayDate});
    }
}
