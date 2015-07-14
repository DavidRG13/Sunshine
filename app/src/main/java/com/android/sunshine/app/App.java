package com.android.sunshine.app;

import android.app.Application;

public class App extends Application {

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        if (component == null) {
            component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        }
    }

    public ApplicationComponent getComponent() {
        return component;
    }

    public void setComponent(final ApplicationComponent component) {
        this.component = component;
    }
}
