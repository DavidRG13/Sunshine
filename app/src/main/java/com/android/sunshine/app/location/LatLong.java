package com.android.sunshine.app.location;

public class LatLong {

    private final double latitude;
    private final double longitude;

    public LatLong(final double latitude, final double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
