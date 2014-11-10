package com.android.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;
import com.android.sunshine.app.model.WeatherContract;

import java.text.DateFormat;
import java.util.Date;

public class Utilities {

    public static String formatTemperature(final double temperature, final boolean isMetric) {
        double temp;
        if (isMetric){
            temp = temperature;
        }else {
            temp = 9 * temperature / 5 + 32;
        }
        return String.format("%.0f", temp);
    }

    public static String formatDate(final String date){
        final Date dateFromDb = WeatherContract.getDateFromDb(date);
        return DateFormat.getDateInstance().format(dateFromDb);
    }

    public static boolean isMetric(final Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_unit_key), context.getString(R.string.prefs_units_imperial)).equals(context.getString(R.string.prefs_units_imperial));
    }
}