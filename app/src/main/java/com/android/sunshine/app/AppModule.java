package com.android.sunshine.app;

import android.content.Context;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.repository.ForecastRepository;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.repository.SQLiteRepository;
import com.android.sunshine.app.sync.Downloader;
import com.android.sunshine.app.sync.NotificationsNotifier;
import com.android.sunshine.app.sync.Notifier;
import com.android.sunshine.app.sync.OWM;
import com.android.sunshine.app.sync.SyncService;
import com.android.sunshine.app.sync.WeatherDataSource;
import com.android.sunshine.app.utils.ManualWeatherJsonParser;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.WeatherJsonParser;
import com.android.sunshine.app.utils.WeatherImageProvider;
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
        return new OWM(downloader);
    }

    @Provides
    public WeatherImageProvider providesWeatherResourceProvider(final Downloader downloader){
        return new OWM(downloader);
    }

    @Provides
    public Notifier providesNotifier(final PreferenceRepository preferenceRepository, final TemperatureFormatter temperatureFormatter, final WeatherImageProvider weatherImageProvider){
        return new NotificationsNotifier(preferenceRepository, appContext, temperatureFormatter, weatherImageProvider);
    }
}
