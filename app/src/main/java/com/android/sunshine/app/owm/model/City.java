package com.android.sunshine.app.owm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class City {

    private int id;
    private String name;
    private String country;
    private Coordinates coord;
    private String population;
    private Sys sys;

    public City() {
    }

    public City(final int id, final String name, final String country, final Coordinates coord, final String population, final Sys sys) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.coord = coord;
        this.population = population;
        this.sys = sys;
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

    @JsonProperty
    public String getPopulation() {
        return population;
    }

    @JsonProperty
    public Sys getSys() {
        return sys;
    }
}
