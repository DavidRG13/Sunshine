package com.android.sunshine.app.owm;

import com.android.sunshine.app.owm.model.OWMResponse;
import com.android.sunshine.app.weather.WeatherFetcher;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

public class RetrofitWeatherFetcher implements WeatherFetcher {

    @Override
    public void forecastForLocation(final String location, final Callback<OWMResponse> callback) {
        OWM restAdapter = new RestAdapter.Builder()
            .setEndpoint(OWM.API_URL)
            .setConverter(new JacksonConverter())
            .build()
            .create(OWM.class);

        restAdapter.fetch(location, "json", "metric", "14", callback);
    }
}
