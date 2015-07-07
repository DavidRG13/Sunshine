package com.android.sunshine.app;

import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.activities.SettingsActivity;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.sync.SyncService;
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
}
