package com.android.sunshine.app.utils;

public class WeatherNotification {

    public static final WeatherNotification INVALID_OBJECT = new WeatherNotification(-1, "", -1, "", "", "", "", "");

    private final int weatherId;
    private final String description;
    private final long forecastDate;
    private final String wind;
    private final String pressure;
    private final String humidity;
    private final String max;
    private final String min;

    public WeatherNotification(final int weatherId, final String description, final long forecastDate, final String wind, final String pressure, final String humidity, final String max, final String min) {
        this.weatherId = weatherId;
        this.description = description;
        this.forecastDate = forecastDate;
        this.wind = wind;
        this.pressure = pressure;
        this.humidity = humidity;
        this.max = max;
        this.min = min;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public String getDescription() {
        return description;
    }

    public long getForecastDate() {
        return forecastDate;
    }

    public String getWind() {
        return wind;
    }

    public String getPressure() {
        return pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public String getMax() {
        return max;
    }

    public String getMin() {
        return min;
    }
}
