package com.android.sunshine.app.widget;

public class TodayForecast {

    public static final TodayForecast INVALID_OBJECT = new TodayForecast(-1, "", "", "");
    private final int iconResourceId;
    private final String description;
    private final String maxTemperature;
    private final String minTemperature;

    public TodayForecast(final int iconResourceId, final String description, final String maxTemperature, final String minTemperature) {
        this.iconResourceId = iconResourceId;
        this.description = description;
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public String getDescription() {
        return description;
    }

    public String getMaxTemperature() {
        return maxTemperature;
    }

    public String getMinTemperature() {
        return minTemperature;
    }
}
