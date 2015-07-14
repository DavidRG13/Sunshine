package com.android.sunshine.app;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {AppModuleMock.class})
public interface ApplicationTestComponent extends ApplicationComponent{

    void inject(MainActivityTest mainActivityTest);
}
