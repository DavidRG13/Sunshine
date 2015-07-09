package com.android.sunshine.app.utils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ApplicationPreferences {

    private boolean useTodayLayout;
    private long initialSelectedDate = -1;

    @Inject
    public ApplicationPreferences() {
    }

    public void useTodayLayout(final boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    public boolean useTodayLayout() {
        return useTodayLayout;
    }

    public void setInitialSelectedDate(final long initialSelectedDate) {
        this.initialSelectedDate = initialSelectedDate;
    }

    public long getInitialSelectedDate() {
        return initialSelectedDate;
    }
}
