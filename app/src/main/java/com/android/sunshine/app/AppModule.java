package com.android.sunshine.app;

import android.app.Application;
import android.content.Context;
import com.android.sunshine.app.location.LocationProvider;
import com.android.sunshine.app.location.PreferenceLocationProvider;
import com.android.sunshine.app.utils.IntentLauncher;
import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides
    Context provideContext() {
        return application.getApplicationContext();
    }

    @Provides
    LocationProvider provideLocationProvider(PreferenceLocationProvider locationProvider) {
        return locationProvider;
    }
}
