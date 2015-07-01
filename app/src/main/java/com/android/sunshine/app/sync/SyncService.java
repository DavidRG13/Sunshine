package com.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.sunshine.app.location.PreferenceLocationProvider;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.utils.UserNotificator;
import com.android.sunshine.app.weather.WeatherRepository;

public class SyncService extends Service{

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new SyncAdapter(new PreferenceLocationProvider(this), new WeatherRepository(this, new UserNotificator(this)), new ServerStatusChanger(this), getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}