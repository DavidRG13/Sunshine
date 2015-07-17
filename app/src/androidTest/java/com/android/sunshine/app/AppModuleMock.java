package com.android.sunshine.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.sunshine.app.fragments.ForecastFragmentWeather;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.location.PreferenceLocationProvider;
import com.android.sunshine.app.weather.WeatherRepository;
import com.android.sunshine.app.utils.DateFormatter;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.owm.RetrofitWeatherFetcher;
import com.android.sunshine.app.db.SQLiteDataSource;
import com.android.sunshine.app.weather.WeatherDataSource;
import com.android.sunshine.app.weather.WeatherFetcher;
import dagger.Module;
import dagger.Provides;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.inject.Named;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

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
    WeatherRepository providesWeatherRepo(DateFormatter dateFormatter) {
        WeatherRepository mock = mock(WeatherRepository.class);
        final GregorianCalendar calendar = new GregorianCalendar();
        ArrayList<ForecastFragmentWeather> value = new ArrayList<>();
        value.add(new ForecastFragmentWeather(001, calendar.getTimeInMillis(), dateFormatter.getFriendlyDay(calendar.getTimeInMillis(), true), dateFormatter.getFriendlyDay(calendar.getTimeInMillis(), false), "sunny",
            "30", "18"));
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        value.add(new ForecastFragmentWeather(001, calendar.getTimeInMillis(), dateFormatter.getFriendlyDay(calendar.getTimeInMillis(), true), dateFormatter.getFriendlyDay(calendar.getTimeInMillis(), false), "sunny",
            "30", "18"));
        Mockito.when(mock.getForecastList()).thenReturn(value);
        return mock;
    }

    @Provides
    ServerStatusChanger providesServerStatusChanger() {
        return new ServerStatusChanger(app.getApplicationContext());
    }

}
