package com.android.sunshine.app.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.R;
import javax.inject.Inject;

public class PreferenceLocationProvider implements LocationProvider {

    private Context context;
    private final SharedPreferences sharedPreferences;

    @Inject
    public PreferenceLocationProvider(final Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getPostCode() {
        return sharedPreferences.getString(context.getString(R.string.pref_location_key), context.getString(R.string.location_default));
    }
}
