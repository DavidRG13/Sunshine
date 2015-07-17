package com.android.sunshine.app.weather;

import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.utils.WeatherDetails;
import com.android.sunshine.app.widget.TodayForecast;
import java.util.ArrayList;
import java.util.List;

public interface WeatherRepository {

    void syncImmediately();

    WeatherDetails getForecastFor(final long date, final String location);

    ArrayList<WeatherDetails> getForecastForDetailWidget();

    TodayForecast getForecastForNowAndCurrentPosition();

    List<ForecastFragmentWeather> getForecastList();
}
