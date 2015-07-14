package com.android.sunshine.app;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.sync.WeatherRepository;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.weather.WeatherDataSource;
import com.android.sunshine.app.weather.WeatherFetcher;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent extends ApplicationComponent {

    SharedPreferences provideSharedPrefs();

    ContentResolver provideContentResolver();

    LocationProvider provideLocationProvider();

    DateFormatter providesDateFormatter();

    WeatherFetcher providesWeatherFetcher();

    WeatherDataSource providesWeatherDataSource();

    WeatherRepository providesWeatherRepo();

    ServerStatusChanger providesServerStatusChanger();
}
