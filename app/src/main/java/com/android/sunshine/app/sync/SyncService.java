package com.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.sunshine.app.SunshineApplication;
import com.android.sunshine.app.repository.PreferenceRepository;
import javax.inject.Inject;

public class SyncService extends Service{

    @Inject
    PreferenceRepository preferenceRepository;

    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        ((SunshineApplication) getApplication()).getObjectGraph().inject(this);
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new SyncAdapter(getApplicationContext(), true, preferenceRepository);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}