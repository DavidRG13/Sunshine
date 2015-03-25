package com.android.sunshine.app;

import android.content.Context;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.repository.ForecastRepository;
import com.android.sunshine.app.repository.SQLiteRepository;
import com.android.sunshine.app.sync.NotificationsUserNotifier;
import com.android.sunshine.app.sync.OWM;
import com.android.sunshine.app.sync.SyncService;
import com.android.sunshine.app.sync.UserNotifier;
import com.android.sunshine.app.sync.WeatherDataSource;
import com.android.sunshine.app.utils.AndroidStringFormatter;
import com.android.sunshine.app.utils.ManualWeatherJsonParser;
import com.android.sunshine.app.utils.StringFormatter;
import com.android.sunshine.app.utils.WeatherImageProvider;
import com.android.sunshine.app.utils.WeatherJsonParser;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

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
    public ForecastRepository providesForecastRepository(final SQLiteRepository forecastRepository) {
        return forecastRepository;
    }

    @Provides
    public WeatherJsonParser providesWeatherJsonParser(final ManualWeatherJsonParser jsonParser) {
        return jsonParser;
    }

    @Provides
    public WeatherDataSource providesWeatherDataSource(final OWM weatherDataSource) {
        return weatherDataSource;
    }

    @Singleton
    @Provides
    public WeatherImageProvider providesWeatherResourceProvider(final OWM weatherImageProvider){
        return weatherImageProvider;
    }

    @Provides
    public UserNotifier providesNotifier(final NotificationsUserNotifier userNotifier){
        return userNotifier;
    }

    @Provides
    public StringFormatter providesStringFormatter(final AndroidStringFormatter stringFormatter) {
        return stringFormatter;
    }
}
