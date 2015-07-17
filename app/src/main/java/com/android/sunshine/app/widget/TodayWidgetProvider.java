package com.android.sunshine.app.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.android.sunshine.app.utils.Navigator;

public class TodayWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId, final Bundle newOptions) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull final Context context, @NonNull final Intent intent) {
        super.onReceive(context, intent);
        if (Navigator.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }
    }
}
