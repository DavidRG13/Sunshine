package com.android.sunshine.app.sync;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.repository.WeatherContract;
import com.android.sunshine.app.utils.TemperatureFormatter;
import com.android.sunshine.app.utils.WeatherImageProvider;
import java.util.Date;
import javax.inject.Inject;

public class NotificationsNotifier implements Notifier {

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
        WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private PreferenceRepository preferenceRepository;
    private Context context;
    private TemperatureFormatter temperatureFormatter;
    private WeatherImageProvider weatherImageProvider;

    @Inject
    public NotificationsNotifier(final PreferenceRepository preferenceRepository, final Context context, final TemperatureFormatter temperatureFormatter, final WeatherImageProvider weatherImageProvider){
        this.preferenceRepository = preferenceRepository;
        this.context = context;
        this.temperatureFormatter = temperatureFormatter;
        this.weatherImageProvider = weatherImageProvider;
    }

    @Override
    public void notifyWeather() {
        if (preferenceRepository.shouldDisplayNotifications()) {
            long lastSync = preferenceRepository.getLastNotification();

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                String locationQuery = preferenceRepository.getLocation();

                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, WeatherContract.getDbDateString(new Date()));

                Cursor cursor = context.getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = weatherImageProvider.getIconResourceForWeatherCondition(weatherId);
                    String title = context.getString(R.string.app_name);

                    String contentText = String.format(context.getString(R.string.format_notification),
                        desc,
                        temperatureFormatter.format(high, preferenceRepository.isMetric()),
                        temperatureFormatter.format(low, preferenceRepository.isMetric()));

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(iconId)
                        .setContentTitle(title)
                        .setContentText(contentText);

                    Intent resultIntent = new Intent(context, MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    preferenceRepository.saveLastNotification();
                }
            }
        }

    }
}
