package com.android.sunshine.app.location;

import android.content.Context;
import com.android.sunshine.app.utils.Utilities;

public class PreferenceLocationProvider implements LocationProvider {

    private Context context;

    public PreferenceLocationProvider(final Context context) {
        this.context = context;
    }

    @Override
    public String getLocation() {
        return Utilities.getLocationSettings(context);
    }
}
