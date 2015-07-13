package com.android.sunshine.app.fragments;

public class ForecastFragmentWeather {
    private final int weatherId;
    private final long dateInMillis;
    private final String longDate;
    private final String shortDate;
    private final String description;
    private final String maxTemp;
    private final String minTemp;

    public ForecastFragmentWeather(final int weatherId, final long dateInMillis, final String longDate, final String shortDate, final String description, final String maxTemp, final String minTemp) {
        this.weatherId = weatherId;
        this.dateInMillis = dateInMillis;
        this.longDate = longDate;
        this.shortDate = shortDate;
        this.description = description;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public String getDescription() {
        return description;
    }

    public String getMaxTemp() {
        return maxTemp;
    }

    public String getMinTemp() {
        return minTemp;
    }

    public String getLongDate() {
        return longDate;
    }

    public String getShortDate() {
        return shortDate;
    }
}
