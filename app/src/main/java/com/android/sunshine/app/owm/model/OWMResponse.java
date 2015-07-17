package com.android.sunshine.app.owm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;

public class OWMResponse {

    private String cod;
    private double message;
    private City city;
    private int cnt;
    private ArrayList<OWMWeatherForecast> list;

    public OWMResponse() {
    }

    public OWMResponse(final String cod, final double message, final City city, final int cnt, final ArrayList<OWMWeatherForecast> list) {
        this.cod = cod;
        this.message = message;
        this.city = city;
        this.cnt = cnt;
        this.list = list;
    }

    @JsonProperty
    public String getCod() {
        return cod;
    }

    @JsonProperty
    public double getMessage() {
        return message;
    }

    @JsonProperty
    public City getCity() {
        return city;
    }

    @JsonProperty
    public int getCnt() {
        return cnt;
    }

    @JsonProperty
    public ArrayList<OWMWeatherForecast> getList() {
        return list;
    }
}
