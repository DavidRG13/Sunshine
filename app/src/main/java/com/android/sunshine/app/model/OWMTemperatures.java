package com.android.sunshine.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OWMTemperatures {

    private double day;
    private double min;
    private double max;
    private double night;
    private double eve;
    private double morn;

    public OWMTemperatures() {
    }

    public OWMTemperatures(final double day, final double min, final double max, final double night, final double eve, final double morn) {
        this.day = day;
        this.min = min;
        this.max = max;
        this.night = night;
        this.eve = eve;
        this.morn = morn;
    }

    @JsonProperty
    public double getDay() {
        return day;
    }

    @JsonProperty
    public double getMin() {
        return min;
    }

    @JsonProperty
    public double getMax() {
        return max;
    }

    @JsonProperty
    public double getNight() {
        return night;
    }

    @JsonProperty
    public double getEve() {
        return eve;
    }

    @JsonProperty
    public double getMorn() {
        return morn;
    }
}
