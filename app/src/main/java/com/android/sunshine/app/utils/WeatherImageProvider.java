package com.android.sunshine.app.utils;

public interface WeatherImageProvider {

    int getIconResourceForWeatherCondition(int weatherId);

    int getArtResourceForWeatherCondition(int weatherId);
}
