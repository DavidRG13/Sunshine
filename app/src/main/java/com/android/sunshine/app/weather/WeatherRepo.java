package com.android.sunshine.app.weather;

import android.content.Context;
import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.owm.model.OWMResponse;
import com.android.sunshine.app.utils.Navigator;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.utils.UserNotificator;
import com.android.sunshine.app.utils.WeatherDetails;
import com.android.sunshine.app.widget.TodayForecast;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class WeatherRepo implements WeatherRepository, Callback<OWMResponse> {

    private final WeatherDataSource weatherDataSource;
    private final Navigator navigator;
    private final LocationProvider locationProvider;
    private final ServerStatusChanger serverStatusChanger;
    private final UserNotificator userNotificator;
    private final WeatherFetcher weatherFetcher;
    private Context context;

    @Inject
    public WeatherRepo(final WeatherDataSource weatherDataSource, final Navigator navigator, final LocationProvider locationProvider, final ServerStatusChanger serverStatusChanger, final UserNotificator userNotificator,
        final WeatherFetcher weatherFetcher, final Context context) {
        this.weatherDataSource = weatherDataSource;
        this.navigator = navigator;
        this.locationProvider = locationProvider;
        this.serverStatusChanger = serverStatusChanger;
        this.userNotificator = userNotificator;
        this.weatherFetcher = weatherFetcher;
        this.context = context;
    }

    @Override
    public void syncImmediately() {
        weatherFetcher.forecastForLocation(locationProvider.getPostCode(), this);
    }

    @Override
    public WeatherDetails getForecastFor(final long date, final String location) {
        return weatherDataSource.getForecastFor(date, location);
    }

    @Override
    public ArrayList<WeatherDetails> getForecastForDetailWidget() {
        return weatherDataSource.getForecastForDetailWidget();
    }

    @Override
    public TodayForecast getForecastForNowAndCurrentPosition() {
        return weatherDataSource.getForecastForNowAndCurrentPosition();
    }

    @Override
    public List<ForecastFragmentWeather> getForecastList() {
        return weatherDataSource.getForecastList();
    }

    @Override
    public void success(final OWMResponse owmResponse, final Response response) {
        final String location = locationProvider.getPostCode();
        int responseCode = Integer.parseInt(owmResponse.getCod());
        serverStatusChanger.fromResponseCode(responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) {
            weatherDataSource.saveWeatherForLocation(owmResponse, location);
            userNotificator.notifyWeather(weatherDataSource.getForecastForNowAndCurrentPosition());
            navigator.updateWidgets(context);
        }
    }

    @Override
    public void failure(final RetrofitError error) {

    }
}
