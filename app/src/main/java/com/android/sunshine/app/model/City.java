package com.android.sunshine.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class City {

    private int id;
    private String name;
    private String country;
    private Coordinates coord;

    public City() {
    }

    public City(final int id, final String name, final String country, final Coordinates coord) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.coord = coord;
    }

    @JsonProperty
    public int getId() {
        return id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getCountry() {
        return country;
    }

    @JsonProperty
    public Coordinates getCoord() {
        return coord;
    }
}
