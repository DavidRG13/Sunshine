package com.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.sunshine.app.SunshineApplication;
import com.android.sunshine.app.repository.ForecastRepository;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.utils.WeatherJsonParser;
import javax.inject.Inject;

public class SyncService extends Service{

    @Inject
    PreferenceRepository preferenceRepository;

    @Inject
    WeatherJsonParser weatherJsonParser;

    @Inject
    ForecastRepository forecastRepository;

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        ((SunshineApplication) getApplication()).getObjectGraph().inject(this);
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new SyncAdapter(getApplicationContext(), true, preferenceRepository, weatherJsonParser, forecastRepository);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}