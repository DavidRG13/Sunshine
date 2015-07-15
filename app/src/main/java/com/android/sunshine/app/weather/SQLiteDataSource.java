package com.android.sunshine.app.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.android.sunshine.app.db.DBHelper;
import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.model.OWMWeather;
import com.android.sunshine.app.model.OWMWeatherForecast;
import com.android.sunshine.app.model.WeatherEntry;
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

    private static final String LOCATION_SETTINGS_WITH_START_DATE_SELECTION = WeatherEntry.COLUMN_LOCATION_SETTINGS + " = ? AND "
        + WeatherEntry.COLUMN_DATE + " >= ? ";
    private static final String LOCATION_SETTINGS_WITH_DAY_SELECTION = WeatherEntry.COLUMN_LOCATION_SETTINGS + " = ? AND "
        + WeatherEntry.COLUMN_DATE + " = ? ";


    private static final String[] COLUMNS = new String[]{
        WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
        WeatherEntry.COLUMN_DATE,
        WeatherEntry.COLUMN_SHORT_DESC,
        WeatherEntry.COLUMN_MAX_TEMP,
        WeatherEntry.COLUMN_MIN_TEMP,
        WeatherEntry.COLUMN_HUMIDITY,
        WeatherEntry.COLUMN_PRESSURE,
        WeatherEntry.COLUMN_WIND_SPEED,
        WeatherEntry.COLUMN_WEATHER_ID
    };

    private static final String[] TODAY_WIDGET_FORECAST_COLUMNS = {
        WeatherEntry.COLUMN_WEATHER_ID,
        WeatherEntry.COLUMN_SHORT_DESC,
        WeatherEntry.COLUMN_MAX_TEMP,
        WeatherEntry.COLUMN_MIN_TEMP
    };

    private static final String[] FORECAST_LIST_COLUMNS = new String[] {
        WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
        WeatherEntry.COLUMN_DATE,
        WeatherEntry.COLUMN_SHORT_DESC,
        WeatherEntry.COLUMN_MAX_TEMP,
        WeatherEntry.COLUMN_MIN_TEMP,
        WeatherEntry.COLUMN_WEATHER_ID,
    };
    private static final String DATE_ASC_ORDER = WeatherEntry.COLUMN_DATE + " ASC";

    private final DateFormatter dateFormatter;
    private final SQLiteDatabase database;
    private LocationProvider locationProvider;
    private ApplicationPreferences applicationPreferences;

    @Inject
    public SQLiteDataSource(final Context context, final DateFormatter dateFormatter, final LocationProvider locationProvider, final ApplicationPreferences applicationPreferences) {
        this.dateFormatter = dateFormatter;
        this.locationProvider = locationProvider;
        this.applicationPreferences = applicationPreferences;
        database = new DBHelper(context).getWritableDatabase();
    }

    @Override
    public WeatherNotification getForecastFor(final long date, final String location) {
        Cursor cursor = database.query(WeatherEntry.TABLE_NAME, COLUMNS, LOCATION_SETTINGS_WITH_DAY_SELECTION, new String[]{location, String.valueOf(date)}, null, null, DATE_ASC_ORDER);
        WeatherNotification weatherNotification = WeatherNotification.INVALID_OBJECT;
        if (cursor.moveToFirst()) {
            final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            final String description = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            final long forecastDate = cursor.getLong(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
            final String wind = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED));
            final String pressure = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE));
            final String humidity = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY));
            final double maxTemp = cursor.getDouble(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            final double minTemp = cursor.getDouble(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
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
        Cursor data = database.query(WeatherEntry.TABLE_NAME, FORECAST_LIST_COLUMNS, LOCATION_SETTINGS_WITH_DAY_SELECTION, new String[]{locationProvider.getPostCode(), String.valueOf(System.currentTimeMillis())}, null, null,
            DATE_ASC_ORDER);

        while (data.moveToNext()) {
            int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            int weatherArtResourceId = OWMWeather.getIconResourceForWeatherCondition(weatherId);
            String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            long dateInMillis = data.getLong(data.getColumnIndex(WeatherEntry.COLUMN_DATE));
            String formattedDate = dateFormatter.getFriendlyDay(dateInMillis, false);
            double maxTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            double minTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
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

        Cursor cursor = database.query(WeatherEntry.TABLE_NAME, FORECAST_LIST_COLUMNS,
            LOCATION_SETTINGS_WITH_START_DATE_SELECTION,
            new String[] {locationProvider.getPostCode(), String.valueOf(new Date().getTime())},
            null, null, DATE_ASC_ORDER);

        while (cursor.moveToNext()) {
            final int weatherId = cursor.getInt(cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            final long weatherDate = cursor.getLong(cursor.getColumnIndex(WeatherEntry.COLUMN_DATE));
            final String descriptionWeather = cursor.getString(cursor.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            final float maxTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            final float minTemp = cursor.getFloat(cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
            forecastList.add(new ForecastFragmentWeather(weatherId, weatherDate, dateFormatter.getFriendlyDay(weatherDate, true),
                dateFormatter.getFriendlyDay(weatherDate, false), descriptionWeather,
                applicationPreferences.getTemperatureUnit().format(maxTemp), applicationPreferences.getTemperatureUnit().format(minTemp)));
        }
        cursor.close();
        return forecastList;
    }

    @Override
    public TodayForecast getForecastForNowAndCurrentPosition() {
        Cursor data = database.query(WeatherEntry.TABLE_NAME, TODAY_WIDGET_FORECAST_COLUMNS, LOCATION_SETTINGS_WITH_DAY_SELECTION, new String[]{locationProvider.getPostCode(), String.valueOf(System.currentTimeMillis())}, null, null,
            DATE_ASC_ORDER);
        TodayForecast todayForecast = TodayForecast.INVALID_OBJECT;
        if (data.moveToFirst()) {
            int weatherId = data.getInt(data.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID));
            int weatherArtResourceId = OWMWeather.getArtResourceForWeatherCondition(weatherId);
            String description = data.getString(data.getColumnIndex(WeatherEntry.COLUMN_SHORT_DESC));
            double maxTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP));
            double minTemp = data.getDouble(data.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP));
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
            removePastForecast();
            ContentValues[] contentValues = toContentValues(weatherForecasts, cityName, cityLatitude, cityLongitude, locationSettings);

            for (ContentValues contentValue : contentValues) {
                database.insert(WeatherEntry.TABLE_NAME, null, contentValue);
            }
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

            weatherValues.put(WeatherEntry.COLUMN_DATE, calendar.getTimeInMillis());
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, weatherForecast.getHumidity());
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, weatherForecast.getPressure());
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, weatherForecast.getSpeed());
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, weatherForecast.getDeg());
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, weatherForecast.getTemp().getMax());
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, weatherForecast.getTemp().getMin());
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, weatherForecast.getWeather().get(0).getDescription());
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherForecast.getWeather().get(0).getId());
            weatherValues.put(WeatherEntry.COLUMN_CITY, cityName);
            weatherValues.put(WeatherEntry.COLUMN_LATITUDE, cityLatitude);
            weatherValues.put(WeatherEntry.COLUMN_LONGITUDE, cityLongitude);
            weatherValues.put(WeatherEntry.COLUMN_LOCATION_SETTINGS, locationSettings);

            contentValues[i] = weatherValues;
        }
        return contentValues;
    }

    private void removePastForecast() {
        database.delete(WeatherEntry.TABLE_NAME, null, null);
    }
}
