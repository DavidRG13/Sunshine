package com.android.sunshine.app;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.location.PreferenceLocationProvider;
import com.android.sunshine.app.sync.SyncAdapter;
import com.android.sunshine.app.sync.WeatherRepository;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.weather.RetrofitWeatherFetcher;
import com.android.sunshine.app.weather.SQLiteDataSource;
import com.android.sunshine.app.weather.WeatherDataSource;
import com.android.sunshine.app.weather.WeatherFetcher;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    Context provideContext() {
        return application.getApplicationContext();
    }

    @Provides
    SharedPreferences provideSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    ContentResolver provideContentResolver() {
        return application.getContentResolver();
    }

    @Provides
    @Named("autoInitialize")
    Boolean provideAutoInitialize() {
        return true;
    }

    @Provides
    LocationProvider provideLocationProvider(PreferenceLocationProvider locationProvider) {
        return locationProvider;
    }

    @Provides
    DateFormatter providesDateFormatter() {
        return new DateFormatter(application.getString(R.string.today), application.getString(R.string.tomorrow));
    }

    @Provides
    WeatherFetcher providesWeatherFetcher() {
        return new RetrofitWeatherFetcher();
    }

    @Provides
    WeatherDataSource providesWeatherDataSource(SQLiteDataSource dataSource) {
        return dataSource;
    }

    @Provides
    WeatherRepository providesWeatherRepo(SyncAdapter weatherRepo) {
        return weatherRepo;
    }

    @Provides
    ServerStatusChanger providesServerStatusChanger() {
        return new ServerStatusChanger(application.getApplicationContext());
    }
}
