package com.android.sunshine.app.weather;

import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.owm.model.OWMResponse;
import com.android.sunshine.app.utils.WeatherDetails;
import com.android.sunshine.app.widget.TodayForecast;
import java.util.ArrayList;
import java.util.List;

public interface WeatherDataSource {

    void saveWeatherForLocation(OWMResponse response, String location);

    TodayForecast getForecastForNowAndCurrentPosition();

    WeatherDetails getForecastFor(long date, String location);

    ArrayList<WeatherDetails> getForecastForDetailWidget();

    List<ForecastFragmentWeather> getForecastList();
}
