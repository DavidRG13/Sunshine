package com.android.sunshine.app.weather;

import com.android.sunshine.app.owm.model.OWMResponse;

public interface WeatherFetcher {

    OWMResponse forecastForLocation(String location);
}
