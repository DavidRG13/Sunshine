package com.android.sunshine.app.weather;

import com.android.sunshine.app.owm.model.OWMResponse;
import retrofit.Callback;

public interface WeatherFetcher {

    void forecastForLocation(String location, Callback<OWMResponse> response);
}
