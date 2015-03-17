package com.android.sunshine.app;

import android.app.Application;
import dagger.ObjectGraph;
import java.util.Arrays;
import java.util.List;

public class SunshineApplication extends Application {

    private ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        objectGraph = ObjectGraph.create(getModules().toArray());
        objectGraph.inject(this);
    }

    public List<Object> getModules() {
        return Arrays.<Object>asList(new AppModule(this));
    }

    public ObjectGraph getObjectGraph() {
        return objectGraph;
    }
}
