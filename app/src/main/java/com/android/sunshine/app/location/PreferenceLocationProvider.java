package com.android.sunshine.app.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;

public class PreferenceLocationProvider implements LocationProvider {

    private Context context;

    public PreferenceLocationProvider(final Context context) {
        this.context = context;
    }

    @Override
    public String getLocation() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_location_key), context.getString(R.string.location_default));
    }
}
