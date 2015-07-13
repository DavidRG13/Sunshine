package com.android.sunshine.app.sync;

import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.utils.WeatherNotification;
import com.android.sunshine.app.widget.ForecastDetailWidget;
import com.android.sunshine.app.widget.TodayForecast;
import java.util.ArrayList;
import java.util.List;

public interface WeatherRepository {

    void syncImmediately();

    WeatherNotification getForecastFor(final long date, final String location);

    ArrayList<ForecastDetailWidget> getForecastForDetailWidget();

    TodayForecast getForecastForNowAndCurrentPosition();

    List<ForecastFragmentWeather> getForecastList();
}
