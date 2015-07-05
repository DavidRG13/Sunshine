package com.android.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;

public class TemperatureFormatter {

    private Context context;

    public TemperatureFormatter(final Context context) {
        this.context = context;
    }

    public String format(final double max, final double min) {
        return context.getString(R.string.format_max_min, format(max), format(min));
    }

    public String format(final double temperature) {
        double temp;
        if (isMetric()) {
            temp = temperature;
        } else {
            temp = 9 * temperature / 5 + 32;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    public boolean isMetric() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key),
            context.getString(R.string.prefs_units_imperial)).equals(context.getString(R.string.prefs_units_imperial));
    }
}
