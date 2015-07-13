package com.android.sunshine.app.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.widget.TodayForecast;
import javax.inject.Inject;

public class UserNotificator {

    private static final long DAY_IN_MILLIS = 86400000;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private Context context;
    private final SharedPreferences preferences;

    @Inject
    public UserNotificator(final Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void notifyWeather(final TodayForecast todayForecast) {
        if (shouldNotify()) {
            String contentText = context.getString(R.string.format_notification, todayForecast.getDescription(), todayForecast.getMaxTemperature(), todayForecast.getMinTemperature());

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(todayForecast.getIconResourceId())
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(contentText);

            Intent resultIntent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

            setLastNotificationTimestamp();
        }
    }

    private boolean shouldNotify() {
        return notificationsEnabled() && hasNotNotifyADayAgo();
    }

    private boolean hasNotNotifyADayAgo() {
        return System.currentTimeMillis() - getLastNotificationTimestamp() >= DAY_IN_MILLIS;
    }

    private boolean notificationsEnabled() {
        return preferences.getBoolean(context.getString(R.string.pref_enable_notifications_key), Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
    }

    public long getLastNotificationTimestamp() {
        return preferences.getLong(context.getString(R.string.pref_last_notification), 0);
    }

    public void setLastNotificationTimestamp() {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(context.getString(R.string.pref_last_notification), System.currentTimeMillis());
        editor.apply();
    }
}
