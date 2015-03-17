package com.android.sunshine.app.utils;

public class LocationProperties {

    public static final LocationProperties INVALID_OBJECT = new LocationProperties("", "", 0L, 0L);
    private final String locationSettings;
    private final String cityName;
    private final double cityLatitude;
    private final double cityLongitude;

    public LocationProperties(final String locationSettings, final String cityName, final double cityLatitude, final double cityLongitude) {
        this.locationSettings = locationSettings;
        this.cityName = cityName;
        this.cityLatitude = cityLatitude;
        this.cityLongitude = cityLongitude;
    }

    public String getLocationSettings() {
        return locationSettings;
    }

    public String getCityName() {
        return cityName;
    }

    public double getCityLatitude() {
        return cityLatitude;
    }

    public double getCityLongitude() {
        return cityLongitude;
    }
}
