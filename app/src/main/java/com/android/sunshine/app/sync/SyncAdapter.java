package com.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.repository.ForecastRepository;
import com.android.sunshine.app.repository.PreferenceRepository;
import com.android.sunshine.app.repository.WeatherContract;
import com.android.sunshine.app.utils.Utilities;
import com.android.sunshine.app.utils.Weather;
import com.android.sunshine.app.utils.WeatherJsonParser;
import java.util.ArrayList;
import java.util.Date;

import static com.android.sunshine.app.repository.WeatherContract.WeatherEntry;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[]{
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private final PreferenceRepository preferenceRepository;
    private final WeatherJsonParser weatherJsonParser;
    private final ForecastRepository forecastRepository;
    private final WeatherDataSource weatherDataSource;

    public SyncAdapter(final Context context, final boolean autoInitialize, final PreferenceRepository preferenceRepository, final WeatherJsonParser weatherJsonParser, final ForecastRepository forecastRepository, final WeatherDataSource weatherDataSource) {
        super(context, autoInitialize);
        this.preferenceRepository = preferenceRepository;
        this.weatherJsonParser = weatherJsonParser;
        this.forecastRepository = forecastRepository;
        this.weatherDataSource = weatherDataSource;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String locationSettings = preferenceRepository.getLocation();
        String jsonResponse = weatherDataSource.getForecastFor(locationSettings);
        long locationId = forecastRepository.addLocation(weatherJsonParser.parseLocation(jsonResponse, locationSettings));
        ArrayList<Weather> weathers = weatherJsonParser.parseWeatherDataFromJson(jsonResponse, locationId);
        forecastRepository.saveWeathers(weathers);
        notifyWeather();
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
        }
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    private void notifyWeather() {
        if (preferenceRepository.shouldDisplayNotifications()) {
            long lastSync = preferenceRepository.getLastNotification();

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                String locationQuery = preferenceRepository.getLocation();

                Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(locationQuery, WeatherContract.getDbDateString(new Date()));

                Cursor cursor = getContext().getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utilities.getIconResourceForWeatherCondition(weatherId);
                    String title = getContext().getString(R.string.app_name);

                    String contentText = String.format(getContext().getString(R.string.format_notification),
                            desc,
                            Utilities.formatTemperature(getContext(), high),
                            Utilities.formatTemperature(getContext(), low));

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext())
                            .setSmallIcon(iconId)
                            .setContentTitle(title)
                            .setContentText(contentText);

                    Intent resultIntent = new Intent(getContext(), MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                            PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    preferenceRepository.saveLastNotification();
                }
            }
        }
    }
}