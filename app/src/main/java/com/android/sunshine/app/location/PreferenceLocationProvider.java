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

    @Override
    public void saveLocation(final double latitude, final double longitude) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putLong("lat", Double.doubleToRawLongBits(latitude));
        edit.putLong("long", Double.doubleToRawLongBits(longitude));
        edit.apply();
    }

    @Override
    public LatLong getLocation() {
        return new LatLong(Double.longBitsToDouble(sharedPreferences.getLong("lat", Double.doubleToLongBits(0))),
            Double.longBitsToDouble(sharedPreferences.getLong("long", Double.doubleToLongBits(0))));
    }
}
