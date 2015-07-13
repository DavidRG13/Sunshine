package com.android.sunshine.app;

import android.content.ContentResolver;
import android.content.SharedPreferences;
import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.activities.SettingsActivity;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.sync.SyncService;
import com.android.sunshine.app.sync.WeatherRepository;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.weather.WeatherDataSource;
import com.android.sunshine.app.weather.WeatherFetcher;
import com.android.sunshine.app.widget.DetailWidgetRemoteViewsService;
import com.android.sunshine.app.widget.TodayWidgetIntentService;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(ForecastFragment forecastFragment);

    void inject(DetailFragment detailFragment);

    void inject(MainActivity mainActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(SyncService syncService);

    void inject(DetailActivity detailActivity);

    void inject(TodayWidgetIntentService todayWidgetIntentService);

    void inject(DetailWidgetRemoteViewsService detailWidgetRemoteViewsService);



    SharedPreferences provideSharedPrefs();

    ContentResolver provideContentResolver();

    LocationProvider provideLocationProvider();

    DateFormatter providesDateFormatter();

    WeatherFetcher providesWeatherFetcher();

    WeatherDataSource providesWeatherDataSource();

    WeatherRepository providesWeatherRepo();

    ServerStatusChanger providesServerStatusChanger();
}
