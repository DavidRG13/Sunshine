package com.android.sunshine.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Coordinates {

    private double lat;
    private double lon;

    public Coordinates() {
    }

    public Coordinates(final double lat, final double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @JsonProperty
    public double getLat() {
        return lat;
    }

    @JsonProperty
    public double getLon() {
        return lon;
    }
}
