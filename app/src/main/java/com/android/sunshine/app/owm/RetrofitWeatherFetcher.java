package com.android.sunshine.app.owm;

import com.android.sunshine.app.owm.model.OWMResponse;
import com.android.sunshine.app.weather.WeatherFetcher;
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
