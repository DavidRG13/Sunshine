package com.android.sunshine.app.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.model.OWMWeatherForecast;

public class UserNotificator {

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private Context context;

    public UserNotificator(final Context context) {
        this.context = context;
    }

    public void notifyWeather(final OWMWeatherForecast owmWeatherForecast) {
        if (Utilities.notificationsEnabled(context)) {
            long lastSync = Utilities.getLastNotification(context);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

                int weatherId = owmWeatherForecast.getWeather().get(0).getId();
                double high = owmWeatherForecast.getTemp().getMax();
                double low = owmWeatherForecast.getTemp().getMin();
                String desc = owmWeatherForecast.getWeather().get(0).getDescription();

                int iconId = Utilities.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);

                String contentText = String.format(context.getString(R.string.format_notification), desc, Utilities.formatTemperature(context, high), Utilities.formatTemperature(context, low));

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(iconId).setContentTitle(title).setContentText(contentText);

                Intent resultIntent = new Intent(context, MainActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                Utilities.setLastNotification(context);
            }
        }
    }
}
