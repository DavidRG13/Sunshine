package com.android.sunshine.app.utils;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.android.sunshine.app.model.WeatherContract;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Vector;

import static com.android.sunshine.app.model.WeatherContract.LocationEntry;
import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class FetchWeatherTask extends AsyncTask<String[], Void, Void> {

    public static final String QUERY_PARAM = "q";
    public static final String MODE_PARAM = "mode";
    public static final String UNITS_PARAM = "units";
    public static final String DAYS_PARAM = "cnt";
    public static final String BASE_URI = "http://api.openweathermap.org/data/2.5/forecast/daily";
    private Context context;

    public FetchWeatherTask(final Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String[]... params) {
        if (params.length == 0) {
            return null;
        }

        final String locationSettings = params[0][0];
        Uri.Builder builder = Uri.parse(BASE_URI).buildUpon()
                .appendQueryParameter(QUERY_PARAM, locationSettings)
                .appendQueryParameter(MODE_PARAM, "json")
                .appendQueryParameter(UNITS_PARAM, "metric")
                .appendQueryParameter(DAYS_PARAM, "14");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String forecastJsonStr = null;

        try {
            final String uri = builder.build().toString();
            URL url = new URL(uri);

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() > 0) {
                    forecastJsonStr = buffer.toString();
                }
                parseWeatherDataFromJson(forecastJsonStr, locationSettings);
            }
        } catch (IOException | JSONException e) {
            Log.e("WeatherRequester", "Error ", e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return null;
    }

    private void parseWeatherDataFromJson(String forecastJsonStr, String locationSettings) throws JSONException {
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWM_LAT = "lon";
        final String OWM_LNG = "lat";
        final String OWM_CITY = "city";
        final String OWM_NAME = "name";
        final String OWM_COORDS = "coord";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_WEATHER_ID = "id";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        final JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        final String cityName = cityJson.getString(OWM_NAME);
        final JSONObject coordsJson = cityJson.getJSONObject(OWM_COORDS);
        final double cityLatitude = coordsJson.getDouble(OWM_LAT);
        final double cityLongitude = coordsJson.getDouble(OWM_LNG);

        final long locationId = addLocation(locationSettings, cityName, cityLatitude, cityLongitude);

        Vector<ContentValues> cVVector = new Vector<>(weatherArray.length());
        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            long dateTime = dayForecast.getLong(OWM_DATETIME);
            final double pressure = dayForecast.getDouble(OWM_PRESSURE);
            final int humidity = dayForecast.getInt(OWM_HUMIDITY);
            final double windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            final double windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            final String description = weatherObject.getString(OWM_DESCRIPTION);
            final int weatherId = weatherObject.getInt(OWM_WEATHER_ID);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT, WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

            cVVector.add(weatherValues);
        }
        if(cVVector.size() > 0) {
            ContentValues[] contentValues = new ContentValues[cVVector.size()];
            cVVector.toArray(contentValues);
            context.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, contentValues);
        }
    }

    private long addLocation(final String locationSettings, final String cityName, final double cityLatitude, final double cityLongitude) {
        ContentValues locationValues = new ContentValues();
        locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSettings);
        locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
        locationValues.put(LocationEntry.COLUMN_COORD_LAT, cityLatitude);
        locationValues.put(LocationEntry.COLUMN_COORD_LONG, cityLongitude);

        final Cursor cursor = context.getContentResolver().query(LocationEntry.CONTENT_URI, new String[]{LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?", new String[]{locationSettings}, null);
        if(cursor.moveToFirst()){
            return cursor.getColumnIndex(LocationEntry._ID);
        }else{
            return ContentUris.parseId(context.getContentResolver().insert(LocationEntry.CONTENT_URI, locationValues));
        }
    }
}