package com.android.sunshine.app.weather;

import com.android.sunshine.app.model.OWMResponse;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

public class RetrofitWeatherFetcher implements WeatherFetcher {

    @Override
    public OWMResponse forecastForLocation(final String location) {
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(OWM.API_URL)
            .setConverter(new JacksonConverter())
            .build();

        OWM weather = restAdapter.create(OWM.class);
        return weather.fetch(location, "json", "metric", "14");
    }
}
