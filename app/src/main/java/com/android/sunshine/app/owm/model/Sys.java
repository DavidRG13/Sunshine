package com.android.sunshine.app.owm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Sys {

    private int population;

    public Sys() {
    }

    public Sys(final int population) {
        this.population = population;
    }

    @JsonProperty
    public int getPopulation() {
        return population;
    }
}
