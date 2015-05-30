package com.android.sunshine.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OWMWeather {

    private int id;
    private String main;
    private String description;
    private String icon;

    public OWMWeather() {
    }

    public OWMWeather(final int id, final String main, final String description, final String icon) {
        this.id = id;
        this.main = main;
        this.description = description;
        this.icon = icon;
    }

    @JsonProperty
    public int getId() {
        return id;
    }

    @JsonProperty
    public String getMain() {
        return main;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    @JsonProperty
    public String getIcon() {
        return icon;
    }
}
