package com.android.sunshine.app.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.android.sunshine.app.R;
import com.android.sunshine.app.activities.MainActivity;
import com.android.sunshine.app.model.OWMResponse;
import com.android.sunshine.app.model.OWMWeatherForecast;
import com.android.sunshine.app.model.WeatherContract;
import com.android.sunshine.app.utils.Utilities;
import com.android.sunshine.app.weather.OWM;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;

import static com.android.sunshine.app.model.WeatherContract.WeatherEntry;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final String ACTION_DATA_UPDATED = "com.example.android.sunshine.app.ACTION_DATA_UPDATED";

    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int WEATHER_NOTIFICATION_ID = 3004;

    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
        WeatherEntry.COLUMN_WEATHER_ID, WeatherEntry.COLUMN_MAX_TEMP, WeatherEntry.COLUMN_MIN_TEMP, WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        final String locationSettings = Utilities.getLocationSettings(getContext());

        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint(OWM.API_URL)
            .setConverter(new JacksonConverter())
            .build();

        OWM weather = restAdapter.create(OWM.class);
        OWMResponse response = weather.fetch(locationSettings, "json", "metric", "14");

        int responseCode = Integer.parseInt(response.getCod());
        if (responseCode == HttpURLConnection.HTTP_OK) {
            parseWeatherDataFromJson(response, locationSettings);
            setServerStatus(getContext(), ServerStatus.SERVER_STATUS_OK);
        } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            setServerStatus(getContext(), ServerStatus.SERVER_STATUS_LOCATION_INVALID);
        } else {
            setServerStatus(getContext(), ServerStatus.SERVER_STATUS_DOWN);
        }
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
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

    private void parseWeatherDataFromJson(OWMResponse owmResponse, String locationSettings) {
        String cityName = owmResponse.getCity().getName();
        double cityLatitude = owmResponse.getCity().getCoord().getLat();
        double cityLongitude = owmResponse.getCity().getCoord().getLon();

        long locationId = addLocation(locationSettings, cityName, cityLatitude, cityLongitude);

        GregorianCalendar calendar = new GregorianCalendar();

        ArrayList<OWMWeatherForecast> weatherForecasts = owmResponse.getList();
        Vector<ContentValues> cVVector = new Vector<>(weatherForecasts.size());
        for (int i = 0; i < weatherForecasts.size(); i++) {
            OWMWeatherForecast weatherForecast = weatherForecasts.get(i);
            ContentValues weatherValues = new ContentValues();
            calendar.add(Calendar.DAY_OF_YEAR, i);

            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATE, calendar.getTimeInMillis());
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, weatherForecast.getHumidity());
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, weatherForecast.getPressure());
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, weatherForecast.getSpeed());
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, weatherForecast.getDeg());
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, weatherForecast.getTemp().getMax());
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, weatherForecast.getTemp().getMin());
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, weatherForecast.getWeather().get(0).getDescription());
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherForecast.getWeather().get(0).getId());

            cVVector.add(weatherValues);
        }

        if (cVVector.size() > 0) {
            ContentValues[] contentValues = new ContentValues[cVVector.size()];
            cVVector.toArray(contentValues);
            getContext().getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, contentValues);
            notifyWeather();
            updateWidgets();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);
            String yesterdayDate = WeatherContract.getDbDateString(cal.getTime());
            getContext().getContentResolver().delete(WeatherEntry.CONTENT_URI, WeatherEntry.COLUMN_DATE + " <= ?", new String[] { yesterdayDate });
        }
    }

    private void updateWidgets() {
        Context context = getContext();
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED).setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private long addLocation(final String locationSettings, final String cityName, final double cityLatitude, final double cityLongitude) {
        ContentValues locationValues = new ContentValues();
        locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSettings);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LAT, cityLatitude);
        locationValues.put(WeatherContract.LocationEntry.COLUMN_COORD_LONG, cityLongitude);

        long result;
        final Cursor cursor = getContext().getContentResolver()
            .query(WeatherContract.LocationEntry.CONTENT_URI, new String[] { WeatherContract.LocationEntry._ID }, WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?", new String[] { locationSettings }, null);
        if (cursor.moveToFirst()) {
            result = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
        } else {
            result = ContentUris.parseId(getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues));
        }
        cursor.close();
        return result;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    private void setServerStatus(final Context context, final @ServerStatus int serverStatus) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(R.string.prefs_server_status), serverStatus);
        editor.apply();
    }

    private void notifyWeather() {
        if (Utilities.displayNotifications(getContext())) {
            long lastSync = Utilities.getLastNotification(getContext());

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                String locationQuery = Utilities.getLocationSettings(getContext());

                Uri weatherUri = WeatherEntry.buildWeatherLocationWithDate(locationQuery, new Date().getTime());

                Cursor cursor = getContext().getContentResolver().query(weatherUri, NOTIFY_WEATHER_PROJECTION, null, null, null);

                if (cursor.moveToFirst()) {
                    int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                    double high = cursor.getDouble(INDEX_MAX_TEMP);
                    double low = cursor.getDouble(INDEX_MIN_TEMP);
                    String desc = cursor.getString(INDEX_SHORT_DESC);

                    int iconId = Utilities.getIconResourceForWeatherCondition(weatherId);
                    String title = getContext().getString(R.string.app_name);

                    String contentText = String.format(getContext().getString(R.string.format_notification), desc, Utilities.formatTemperature(getContext(), high), Utilities.formatTemperature(getContext(), low));

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext()).setSmallIcon(iconId).setContentTitle(title).setContentText(contentText);

                    Intent resultIntent = new Intent(getContext(), MainActivity.class);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
                    stackBuilder.addNextIntent(resultIntent);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(resultPendingIntent);

                    NotificationManager mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                    Utilities.setLastNotification(getContext());
                    cursor.close();
                }
            }
        }
    }
}