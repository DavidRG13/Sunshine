package com.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.sunshine.app.location.PreferenceLocationProvider;
import com.android.sunshine.app.utils.ServerStatusChanger;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.UserNotificator;
import com.android.sunshine.app.weather.RetrofitWeatherFetcher;
import com.android.sunshine.app.weather.WeatherRepository;

public class SyncService extends Service {

    private static final Object S_SYNC_ADAPTER_LOCK = new Object();
    private static SyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (S_SYNC_ADAPTER_LOCK) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new SyncAdapter(new PreferenceLocationProvider(this),
                    new WeatherRepository(this, new UserNotificator(this, new TemperatureFormatter(this))), new ServerStatusChanger(this), new RetrofitWeatherFetcher(), getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}
