package com.android.sunshine.app.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.sunshine.app.App;
import javax.inject.Inject;

public class SyncService extends Service {

    @Inject
    SyncAdapter syncAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        ((App) getApplication()).getComponent().inject(this);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
