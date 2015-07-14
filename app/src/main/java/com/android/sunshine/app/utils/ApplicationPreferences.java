package com.android.sunshine.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApplicationPreferences {

    private boolean useTodayLayout;
    private long initialSelectedDate = -1;
    private TemperatureUnit temperatureUnit;
    private Context context;

    @Inject
    public ApplicationPreferences(final Context context) {
        this.context = context;
    }

    public void useTodayLayout(final boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    public boolean useTodayLayout() {
        return useTodayLayout;
    }

    public void setInitialSelectedDate(final long initialSelectedDate) {
        this.initialSelectedDate = initialSelectedDate;
    }

    public long getInitialSelectedDate() {
        return initialSelectedDate;
    }

    public void setTemperatureUnit(final TemperatureUnit temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public TemperatureUnit getTemperatureUnit() {
        if (temperatureUnit == null) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            temperatureUnit = TemperatureUnit.fromString(prefs.getString(context.getString(R.string.pref_unit_key), context.getString(R.string.prefs_units_imperial)));
        }
        return temperatureUnit;
    }
}
