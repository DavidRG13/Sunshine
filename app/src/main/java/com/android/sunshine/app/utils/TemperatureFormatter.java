package com.android.sunshine.app.utils;

import javax.inject.Inject;

public class TemperatureFormatter {

    @Inject
    public TemperatureFormatter() {
    }

    public String format(final double temperature, final boolean isMetric) {
        double temp;
        if (isMetric){
            temp = temperature;
        }else {
            temp = convertToImperial(temperature);
        }
        return String.format("%.0f°", temp);
    }

    private double convertToImperial(final double temperature) {
        return 9 * temperature / 5 + 32;
    }
}
