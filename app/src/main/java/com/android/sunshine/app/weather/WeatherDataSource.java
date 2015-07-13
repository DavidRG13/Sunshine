package com.android.sunshine.app.weather;

import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.utils.WeatherNotification;
import com.android.sunshine.app.widget.ForecastDetailWidget;
import com.android.sunshine.app.widget.TodayForecast;
import java.util.ArrayList;
import java.util.List;

public interface WeatherDataSource {

    void saveWeatherForLocation(OWMResponse response, String location);

    TodayForecast getForecastForNowAndCurrentPosition();

    WeatherNotification getForecastFor(long date, String location);

    ArrayList<ForecastDetailWidget> getForecastForDetailWidget();

    List<ForecastFragmentWeather> getForecastList();
}
