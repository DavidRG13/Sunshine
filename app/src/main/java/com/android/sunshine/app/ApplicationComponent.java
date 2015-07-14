package com.android.sunshine.app;

import com.android.sunshine.app.activities.DetailActivity;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.activities.SettingsActivity;
import com.android.sunshine.app.fragments.DetailFragment;
import com.android.sunshine.app.fragments.ForecastFragment;
import com.android.sunshine.app.sync.SyncService;
import com.android.sunshine.app.widget.DetailWidgetRemoteViewsService;
import com.android.sunshine.app.widget.TodayWidgetIntentService;

public interface ApplicationComponent {

    void inject(ForecastFragment forecastFragment);

    void inject(DetailFragment detailFragment);

    void inject(MainActivity mainActivity);

    void inject(SettingsActivity settingsActivity);

    void inject(SyncService syncService);

    void inject(DetailActivity detailActivity);

    void inject(TodayWidgetIntentService todayWidgetIntentService);

    void inject(DetailWidgetRemoteViewsService detailWidgetRemoteViewsService);
}
