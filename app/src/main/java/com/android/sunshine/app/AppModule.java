package com.android.sunshine.app;

import android.content.Context;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.repository.ForecastRepository;
import com.android.sunshine.app.repository.SQLiteRepository;
import com.android.sunshine.app.sync.Downloader;
import com.android.sunshine.app.sync.OWMDataSource;
import com.android.sunshine.app.sync.SyncService;
import com.android.sunshine.app.sync.WeatherDataSource;
import com.android.sunshine.app.utils.ManualWeatherJsonParser;
import com.android.sunshine.app.utils.WeatherJsonParser;
import dagger.Module;
import dagger.Provides;

@Module(
    injects = {
        SunshineApplication.class, ForecastFragment.class, DetailFragment.class, SyncService.class
    },
    library = true)
public class AppModule {

    private Context appContext;

    public AppModule(final Context context) {
        appContext = context;
    }

    @Provides
    public Context providesContext() {
        return appContext;
    }

    @Provides
    public ForecastRepository providesForecastRepository() {
        return new SQLiteRepository(appContext);
    }

    @Provides
    public WeatherJsonParser providesWeatherJsonParser() {
        return new ManualWeatherJsonParser();
    }

    @Provides
    public WeatherDataSource providesWeatherDataSource(final Downloader downloader) {
        return new OWMDataSource(downloader);
    }
}
