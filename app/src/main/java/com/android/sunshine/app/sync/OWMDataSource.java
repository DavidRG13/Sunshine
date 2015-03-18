package com.android.sunshine.app.sync;

import android.net.Uri;

public class OWMDataSource implements WeatherDataSource {

    public static final String QUERY_PARAM = "q";
    public static final String MODE_PARAM = "mode";
    public static final String UNITS_PARAM = "units";
    public static final String DAYS_PARAM = "cnt";
    public static final String BASE_URI = "http://api.openweathermap.org/data/2.5/forecast/daily";

    private Downloader downloader;

    public OWMDataSource(final Downloader downloader) {
        this.downloader = downloader;
    }

    @Override
    public String getForecastFor(final String location) {
        Uri.Builder builder = Uri.parse(BASE_URI).buildUpon()
            .appendQueryParameter(QUERY_PARAM, location)
            .appendQueryParameter(MODE_PARAM, "json")
            .appendQueryParameter(UNITS_PARAM, "metric")
            .appendQueryParameter(DAYS_PARAM, "14");

        return downloader.fromUri(builder.build());
    }
}
