package com.android.sunshine.app;

import com.android.sunshine.app.fragments.ForecastFragment;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(ForecastFragment forecastFragment);
}
