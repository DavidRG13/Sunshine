package com.android.sunshine.app.utils;

import android.util.Log;
import com.android.sunshine.app.repository.WeatherContract;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ManualWeatherJsonParser implements WeatherJsonParser {

    private final String OWM_LIST = "list";
    private final String OWM_WEATHER = "weather";
    private final String OWM_TEMPERATURE = "temp";
    private final String OWM_MAX = "max";
    private final String OWM_MIN = "min";
    private final String OWM_DATETIME = "dt";
    private final String OWM_DESCRIPTION = "main";
    private final String OWM_LAT = "lon";
    private final String OWM_LNG = "lat";
    private final String OWM_CITY = "city";
    private final String OWM_NAME = "name";
    private final String OWM_COORDS = "coord";
    private final String OWM_PRESSURE = "pressure";
    private final String OWM_HUMIDITY = "humidity";
    private final String OWM_WINDSPEED = "speed";
    private final String OWM_WIND_DIRECTION = "deg";
    private final String OWM_WEATHER_ID = "id";

    @Override
    public ArrayList<Weather> parseWeatherDataFromJson(final String forecast, final long locationId) {
        ArrayList<Weather> weathers = new ArrayList<>();
        try {
            JSONArray weatherArray = new JSONObject(forecast).getJSONArray(OWM_LIST);

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

                Weather weather = new Weather(weatherId, description, WeatherContract.getDbDateString(new Date(dateTime * 1000l)), humidity, pressure, windSpeed, windDirection, high, low, locationId);
                weathers.add(weather);
            }
        } catch (JSONException e) {
            Log.e(this.getClass().getName(), "Hubo un problema deserializando");
        }
        return weathers;
    }

    @Override
    public LocationProperties parseLocation(final String forecastJsonStr, final String locationSettings) {
        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            final JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            final String cityName = cityJson.getString(OWM_NAME);
            final JSONObject coordsJson = cityJson.getJSONObject(OWM_COORDS);
            final double cityLatitude = coordsJson.getDouble(OWM_LAT);
            final double cityLongitude = coordsJson.getDouble(OWM_LNG);
            return new LocationProperties(locationSettings, cityName, cityLatitude, cityLongitude);
        } catch (JSONException e) {
            e.printStackTrace();
            return  LocationProperties.INVALID_OBJECT;
        }
    }
}
