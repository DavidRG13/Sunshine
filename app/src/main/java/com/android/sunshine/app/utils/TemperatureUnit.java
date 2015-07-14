package com.android.sunshine.app.utils;

public enum TemperatureUnit {

    IMPERIAL, METRIC;

    public String format(final double temp) {
        if (this == IMPERIAL) {
            return String.format("%1.0f°", 9 * temp / 5 + 32);
        } else if (this == METRIC) {
            return String.format("%1.0f°", temp);
        }
        return "";
    }

    public static TemperatureUnit fromString(final String name) {
        return valueOf(name.toUpperCase());
    }
}
