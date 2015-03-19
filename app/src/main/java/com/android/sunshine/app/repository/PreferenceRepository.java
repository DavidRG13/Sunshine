package com.android.sunshine.app.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;
import javax.inject.Inject;

public class PreferenceRepository {

    private Context context;
    private final SharedPreferences sharedPreferences;

    @Inject
    public PreferenceRepository(final Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getLocation() {
        return sharedPreferences.getString(context.getString(R.string.pref_location_key), context.getString(R.string.location_default));
    }

    public long getLastNotification() {
        return sharedPreferences.getLong(context.getString(R.string.pref_last_notification), 0);
    }

    public void saveLastNotification() {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis());
        editor.apply();
    }

    public boolean shouldDisplayNotifications() {
        String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
        return sharedPreferences.getBoolean(displayNotificationsKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
    }

    public boolean isMetric() {
        return sharedPreferences.getString(context.getString(R.string.pref_unit_key), context.getString(R.string.prefs_units_imperial)).equals(context.getString(R.string.prefs_units_imperial));
    }
}
