package com.android.sunshine.app.utils;

import android.content.Context;
import com.android.sunshine.app.R;
import javax.inject.Inject;

public class AndroidStringFormatter implements StringFormatter{

    private Context context;

    @Inject
    public AndroidStringFormatter(final Context context) {
        this.context = context;
    }

    @Override
    public String getFriendlyToday(final String date) {
        return context.getString(
            R.string.format_full_friendly_date,
            context.getString(R.string.today),
            date);
    }

    @Override
    public String getToday() {
        return context.getString(R.string.today);
    }

    @Override
    public String getTomorrow() {
        return context.getString(R.string.tomorrow);
    }
}
