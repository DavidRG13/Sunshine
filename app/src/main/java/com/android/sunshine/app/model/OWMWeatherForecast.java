package com.android.sunshine.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

public class OWMWeatherForecast {

    private int dt;
    private OWMTemperatures temp;
    private double pressure;
    private int humidity;
    private ArrayList<OWMWeather> weather;
    private double speed;
    private int deg;
    private int clouds;
    private double rain;

    public OWMWeatherForecast() {
    }

    public OWMWeatherForecast(final int dt, final OWMTemperatures temp, final double pressure, final int humidity,
        final ArrayList<OWMWeather> weather, final double speed, final int deg, final int clouds,
        final double rain) {
        this.dt = dt;
        this.temp = temp;
        this.pressure = pressure;
        this.humidity = humidity;
        this.weather = weather;
        this.speed = speed;
        this.deg = deg;
        this.clouds = clouds;
        this.rain = rain;
    }

    @JsonProperty
    public int getDt() {
        return dt;
    }

    @JsonProperty
    public OWMTemperatures getTemp() {
        return temp;
    }

    @JsonProperty
    public double getPressure() {
        return pressure;
    }

    @JsonProperty
    public int getHumidity() {
        return humidity;
    }

    @JsonProperty
    public ArrayList<OWMWeather> getWeather() {
        return weather;
    }

    @JsonProperty
    public double getSpeed() {
        return speed;
    }

    @JsonProperty
    public int getDeg() {
        return deg;
    }

    @JsonProperty
    public int getClouds() {
        return clouds;
    }

    @JsonProperty
    public double getRain() {
        return rain;
    }
}
