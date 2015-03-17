package com.android.sunshine.app.utils;

public class Weather {

    private int id;
    private String description;
    private String date;
    private int humidity;
    private double pressure;
    private double windSpeed;
    private double windDirection;
    private double highTemp;
    private double lowTemp;
    private long locationId;

    public Weather(final int id, final String description, final String date, final int humidity, final double pressure, final double windSpeed, final double windDirection, final double highTemp,
        final double lowTemp, final long locationId) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.locationId = locationId;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public double getHighTemp() {
        return highTemp;
    }

    public double getLowTemp() {
        return lowTemp;
    }

    public long getLocationId() {
        return locationId;
    }
}
