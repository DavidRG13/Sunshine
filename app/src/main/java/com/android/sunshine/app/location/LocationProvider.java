package com.android.sunshine.app.location;

public interface LocationProvider {
    String getPostCode();

    void saveLocation(double latitude, double longitude);

    LatLong getLocation();
}
