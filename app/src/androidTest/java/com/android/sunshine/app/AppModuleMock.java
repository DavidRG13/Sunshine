package com.android.sunshine.app;

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
public class AppModuleMock {

    private App app;

    public AppModuleMock(final App app) {
        this.app = app;
    }

    @Provides
    LocationProvider provideLocationProvider() {
        return new PreferenceLocationProvider(app.getApplicationContext());
    }

    @Provides
    Context provideContext() {
        return app.getApplicationContext();
    }

    @Provides
    SharedPreferences provideSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    ContentResolver provideContentResolver() {
        return app.getContentResolver();
    }

    @Provides
    @Named("autoInitialize")
    Boolean provideAutoInitialize() {
        return true;
    }

    @Provides
    DateFormatter providesDateFormatter() {
        return new DateFormatter(app.getString(R.string.today), app.getString(R.string.tomorrow));
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
        return new ServerStatusChanger(app.getApplicationContext());
    }

}
